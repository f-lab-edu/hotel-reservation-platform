package com.reservation.customer.payment.service;

import static com.reservation.support.utils.retry.OptimisticLockingFailureRetryUtils.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reservation.customer.payment.repository.JpaPaymentRepository;
import com.reservation.customer.payment.service.dto.IamportPayment;
import com.reservation.customer.payment.service.iamport.Iamport;
import com.reservation.customer.reservation.repository.JpaReservationRepository;
import com.reservation.customer.roomavailabilitysummary.repository.JpaRoomAvailabilitySummaryRepository;
import com.reservation.domain.payment.Payment;
import com.reservation.domain.payment.enums.PaymentStatus;
import com.reservation.domain.reservation.Reservation;
import com.reservation.domain.reservation.enums.ReservationStatus;
import com.reservation.domain.roomavailabilitysummary.RoomAvailabilitySummary;
import com.reservation.support.exception.ErrorCode;
import com.siot.IamportRestClient.request.CancelData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
	private static final int MAX_OPTIMISTIC_RETRY_COUNT = 5;
	private static final long MAX_LOCK_WAIT_TIME_SECONDS = 10L; // 락 대기 최대 시간
	private static final long LOCK_WAIT_TIME_SECONDS = 5L;

	private final Iamport iamport;

	private final JpaPaymentRepository paymentRepository;
	private final JpaReservationRepository reservationRepository;
	private final JpaRoomAvailabilitySummaryRepository jpaAvailabilitySummaryRepository;
	// private final RoomAvailabilitySummaryRepository availabilitySummaryRepository;

	private final RedissonClient redisson;

	// 결제 검증 과정에서 낙관적 락을 사용하여 예약 가능 수량을 증가시키는 방식
	@Transactional
	public IamportPayment optimisticValidationPayment(String paymentUid, long reservationId) {
		IamportPayment iamportPayment;

		iamportPayment = new IamportPayment(iamport.paymentByImpUid(paymentUid));

		// 결제 완료가 아니면
		if (!iamportPayment.payment().getStatus().equals("paid")) {
			throw ErrorCode.CONFLICT.exception("결제 미완료 상태입니다. 다시 결제해주세요");
		}

		// 예약 조회
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> ErrorCode.CONFLICT.exception("예약된 내역이 없습니다."));

		// 결제해야할 예약 금액
		int reservationTotalPrice = reservation.getTotalPrice();
		// 실 결제 금액
		int iamportPrice = iamportPayment.payment().getAmount().intValue();

		Payment payment = Payment.builder()
			.paymentUid(paymentUid)
			.price(iamportPrice)
			.status(PaymentStatus.INITIATED)
			.build();

		// 만약 10분 초과로 이미 예약이 취소된 경우
		if (reservation.getStatus().equals(ReservationStatus.CANCELED)) {
			// 이미 취소된 예약 -> 결제금액 취소(아임포트)
			payment.markCancelled();
			paymentRepository.save(payment);
			return optimisticCancelPayment(reservation, iamportPayment.payment().getImpUid(), iamportPrice);
		}

		// 만약 10분 초과로 이미 예약이 취소된 경우 또는 결제 금액이 맞지 않는 경우
		if (iamportPrice != reservationTotalPrice) {
			// 결제금액 위변조로 의심 -> 결제금액 취소(아임포트) 및 예약 취소 & 예약 수량 원복
			payment.markCancelled();
			paymentRepository.save(payment);
			return optimisticCancelPayment(reservation, iamportPayment.payment().getImpUid(), iamportPrice);
		}

		// 결제 완료
		payment.markCompleted();
		paymentRepository.save(payment);

		// 예약 완료
		reservation.markConfirmed();
		reservationRepository.save(reservation);
		return iamportPayment;
	}

	// 아임포트 결제 취소 처리 -> 낙관적 락 기반
	private IamportPayment optimisticCancelPayment(Reservation reservation, String paymentUid, int amount) {
		// 1. 예약 취소
		reservation.markCanceled();
		reservationRepository.save(reservation);

		// 2. 예약 가능 수량 증가 (낙관적 락 기반)
		executeWithRetry(MAX_OPTIMISTIC_RETRY_COUNT, () -> increaseAvailabilityCount(reservation));

		// 3. 결제 취소
		CancelData cancelData = new CancelData(paymentUid, true, new BigDecimal(amount));
		return new IamportPayment(iamport.cancelPaymentByImpUid(cancelData));
	}

	// 예약 가능 수량 증가
	private RoomAvailabilitySummary increaseAvailabilityCount(Reservation reservation) {

		long roomTypeId = reservation.getRoomTypeId();
		LocalDate checkIn = reservation.getCheckIn();
		LocalDate checkOut = reservation.getCheckOut();

		RoomAvailabilitySummary availabilitySummary =
			jpaAvailabilitySummaryRepository.findOneByRoomTypeIdAndCheckInDate(roomTypeId, checkIn)
				.orElseThrow(() -> ErrorCode.CONFLICT.exception("예약 가능 수량이 없습니다."));

		int requiredDayCount = (int)ChronoUnit.DAYS.between(checkIn, checkOut);
		availabilitySummary.increaseAvailability(requiredDayCount);
		availabilitySummary.prePersist();

		// 버전 기반 낙관적 락으로 saveAll 시점에 충돌 검증
		return jpaAvailabilitySummaryRepository.save(availabilitySummary);
	}

	// 결제 검증 과정에서 레디슨 락을 사용하여 예약 가능 수량을 증가시키는 방식
	@Transactional
	public IamportPayment redissonValidationPayment(String paymentUid, long reservationId) {
		IamportPayment iamportPayment;

		iamportPayment = new IamportPayment(iamport.paymentByImpUid(paymentUid));

		// 결제 완료가 아니면
		if (!iamportPayment.payment().getStatus().equals("paid")) {
			throw ErrorCode.CONFLICT.exception("결제 미완료 상태입니다. 다시 결제해주세요");
		}

		// 혹시 모를 데드락을 줄이기 위해, 멀티락 X -> 순서대로 락을 획득하는 방식으로 변경
		// 비관적 락 처리 시작
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> ErrorCode.CONFLICT.exception("예약된 내역이 없습니다."));
		RLock reservationLock = redisson.getLock("reservation:lock:" + reservationId);
		boolean isReservationLocked = false;

		long roomTypeId = reservation.getRoomTypeId();
		RLock checkInLock = redisson.getLock("reservation:lock:" + roomTypeId + ":" + reservation.getCheckIn());
		boolean isCheckInLock = false;

		try {
			isReservationLocked =
				reservationLock.tryLock(MAX_LOCK_WAIT_TIME_SECONDS, LOCK_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
			if (!isReservationLocked) {
				throw ErrorCode.CONFLICT.exception("해당 예약 건은 현재 다른 결제 처리 중입니다. 잠시 후 다시 시도해 주세요.");
			}
			isCheckInLock = checkInLock.tryLock(MAX_LOCK_WAIT_TIME_SECONDS, LOCK_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
			if (!isCheckInLock) {
				throw ErrorCode.CONFLICT.exception("해당 예약 건의 체크인 월은 현재 다른 결제 처리 중입니다. 잠시 후 다시 시도해 주세요.");
			}

			// 결제해야할 예약 금액
			int reservationTotalPrice = reservation.getTotalPrice();
			// 실 결제 금액
			int iamportPrice = iamportPayment.payment().getAmount().intValue();

			Payment payment = Payment.builder()
				.paymentUid(paymentUid)
				.price(iamportPrice)
				.status(PaymentStatus.INITIATED)
				.build();

			// 10분 초과로 이미 예약이 취소된 경우
			if (reservation.getStatus().equals(ReservationStatus.CANCELED)) {
				// 이미 취소된 예약 -> 결제금액 취소(아임포트)
				payment.markCancelled();
				paymentRepository.save(payment);
				CancelData cancelData = new CancelData(paymentUid, true, new BigDecimal(iamportPrice));
				return new IamportPayment(iamport.cancelPaymentByImpUid(cancelData));
			}

			// 결제 금액이 맞지 않는 경우
			if (iamportPrice != reservationTotalPrice) {
				// 결제금액 위변조로 의심 -> 결제금액 취소(아임포트) 및 예약 취소 & 예약 수량 원복
				payment.markCancelled();
				paymentRepository.save(payment);
				return redissonCancelPayment(reservation, iamportPayment.payment().getImpUid(), iamportPrice);
			}

			// 결제 완료
			payment.markCompleted();
			paymentRepository.save(payment);

			// 예약 완료
			reservation.markConfirmed();
			reservationRepository.save(reservation);
			return iamportPayment;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw ErrorCode.CONFLICT.exception("결제 검증 처리 중 오류 발생");
		} finally {
			if (isCheckInLock) {
				checkInLock.unlock();
			}
			if (isReservationLocked) {
				reservationLock.unlock();
			}
		}
	}

	// 아임포트 결제 취소 처리: 레디슨 락(비관적) 기반이기 때문에 retry 없이 바로 처리
	private IamportPayment redissonCancelPayment(Reservation reservation, String paymentUid, int amount) {
		// 1. 예약 취소
		reservation.markCanceled();
		reservationRepository.save(reservation);

		// 2. 예약 가능 수량 증가
		increaseAvailabilityCount(reservation);

		// 3. 결제 취소
		CancelData cancelData = new CancelData(paymentUid, true, new BigDecimal(amount));
		return new IamportPayment(iamport.cancelPaymentByImpUid(cancelData));
	}
}

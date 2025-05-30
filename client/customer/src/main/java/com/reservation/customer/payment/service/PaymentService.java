package com.reservation.customer.payment.service;

import static com.reservation.support.utils.retry.OptimisticLockingFailureRetryUtils.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reservation.customer.payment.repository.JpaPaymentRepository;
import com.reservation.customer.payment.service.dto.IamportPayment;
import com.reservation.customer.payment.service.iamport.Iamport;
import com.reservation.customer.reservation.repository.JpaReservationRepository;
import com.reservation.customer.roomavailability.repository.JpaRoomAvailabilityRepository;
import com.reservation.domain.payment.Payment;
import com.reservation.domain.payment.enums.PaymentStatus;
import com.reservation.domain.reservation.Reservation;
import com.reservation.domain.reservation.enums.ReservationStatus;
import com.reservation.domain.roomavailability.OriginRoomAvailability;
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
	private final JpaRoomAvailabilityRepository roomAvailabilityRepository;

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
	private List<OriginRoomAvailability> increaseAvailabilityCount(Reservation reservation) {
		List<OriginRoomAvailability> availabilities =
			roomAvailabilityRepository.findAllByRoomTypeIdAndOpenDateBetween(
				reservation.getRoomTypeId(), reservation.getCheckIn(), reservation.getCheckOut().minusDays(1)
			);

		for (OriginRoomAvailability availability : availabilities) {
			availability.increaseAvailableCount();
		}

		// 버전 기반 낙관적 락으로 saveAll 시점에 충돌 검증
		return roomAvailabilityRepository.saveAll(availabilities);
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

		// 2. 비관적 락 처리 시작
		RLock lock = getReservationLock(reservationId);
		boolean isLocked = false;
		try {
			isLocked = lock.tryLock(MAX_LOCK_WAIT_TIME_SECONDS, LOCK_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
			if (!isLocked) {
				throw ErrorCode.CONFLICT.exception("해당 예약 건은 현재 다른 결제 처리 중입니다. 잠시 후 다시 시도해 주세요.");
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
			if (isLocked) {
				lock.unlock();
			}
		}
	}

	private RLock getReservationLock(long reservationId) {
		String lockKey = "reservation:lock:" + reservationId;
		return redisson.getLock(lockKey);
	}

	// 아임포트 결제 취소 처리 -> 낙관적 락 기반
	private IamportPayment redissonCancelPayment(Reservation reservation, String paymentUid, int amount) {
		// 1. 예약 취소
		reservation.markCanceled();
		reservationRepository.save(reservation);

		// 2. 예약 가능 수량 증가 (redisson 멀티 락 기반)
		RLock lock = getReservationPeriodLock(reservation);
		boolean isLocked = false;
		try {
			isLocked = lock.tryLock(MAX_LOCK_WAIT_TIME_SECONDS, LOCK_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
			if (!isLocked) {
				throw ErrorCode.CONFLICT.exception("해당 객실은 현재 다른 예약 중에 있습니다. 잠시 후 다시 시도해 주세요.");
			}

			// 2. 예약 가능 수량 증가
			increaseAvailabilityCount(reservation);

			// 3. 결제 취소
			CancelData cancelData = new CancelData(paymentUid, true, new BigDecimal(amount));
			return new IamportPayment(iamport.cancelPaymentByImpUid(cancelData));
		} catch (Exception e) {
			log.error(e.getMessage());
			throw ErrorCode.CONFLICT.exception("예약 취소 및 가능 수량 증가 중 오류 발생: " + e.getMessage());
		} finally {
			if (isLocked) {
				lock.unlock();
			}
		}
	}

	private RLock getReservationPeriodLock(Reservation reservation) {
		List<RLock> locks = new ArrayList<>();
		LocalDate checkIn = reservation.getCheckIn();
		LocalDate checkOut = reservation.getCheckOut();

		for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(
			1)) {
			String key = "reservation:lock:" + reservation.getRoomTypeId() + ":" + date;
			locks.add(redisson.getLock(key));
		}

		return new RedissonMultiLock(locks.toArray(new RLock[0]));
	}
}

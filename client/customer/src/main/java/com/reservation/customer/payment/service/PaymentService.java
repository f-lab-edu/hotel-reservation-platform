package com.reservation.customer.payment.service;

import static com.reservation.support.utils.retry.OptimisticLockingFailureRetryUtils.*;

import java.math.BigDecimal;
import java.util.List;

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
import com.reservation.domain.roomavailability.OriginRoomAvailability;
import com.reservation.support.exception.ErrorCode;
import com.siot.IamportRestClient.request.CancelData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
	private final Iamport iamport;
	private final JpaPaymentRepository paymentRepository;
	private final JpaReservationRepository reservationRepository;
	private final JpaRoomAvailabilityRepository roomAvailabilityRepository;

	@Transactional
	public IamportPayment validationPayment(String paymentUid, long reservationId) {
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

		// 결제 금액 검증
		if (iamportPrice != reservationTotalPrice) {
			// 결제금액 위변조로 의심되는 결제금액을 취소(아임포트)
			payment.markCancelled();
			paymentRepository.save(payment);
			return cancelPayment(reservation, iamportPayment.payment().getImpUid(), iamportPrice);
		}

		// 결제 완료
		payment.markCompleted();
		paymentRepository.save(payment);
		
		// 예약 완료
		reservation.markConfirmed();
		reservationRepository.save(reservation);
		return iamportPayment;
	}

	// 아임포트 결제 취소 처리
	private IamportPayment cancelPayment(Reservation reservation, String paymentUid, int amount) {
		// 1. 예약 취소
		reservation.markCanceled();
		reservationRepository.save(reservation);

		// 2. 예약 가능 수량 증가 (낙관적 락 기반)
		executeWithRetry(5, () -> increaseAvailabilityCount(reservation));

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
}

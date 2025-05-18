package com.reservation.customer.reservation.service;

import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reservation.customer.reservation.repository.JpaReservationRepository;
import com.reservation.customer.reservation.service.dto.CreateReservationCommand;
import com.reservation.customer.reservation.service.dto.CreateReservationResult;
import com.reservation.customer.roomavailability.repository.JpaRoomAvailabilityRepository;
import com.reservation.domain.reservation.Reservation;
import com.reservation.domain.reservation.enums.ReservationStatus;
import com.reservation.domain.roomavailability.OriginRoomAvailability;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {
	private final JpaReservationRepository reservationRepository;
	private final JpaRoomAvailabilityRepository roomAvailabilityRepository;

	@Transactional
	public CreateReservationResult createReservation(Long memberId, CreateReservationCommand command) {
		// 1. 예약 가능 여부 확인
		validateAvailability(command);

		// 2. 수량 차감 (낙관적 락 기반)
		decreaseAvailabilityCount(command);

		// 3. 총 숙박 요금 계산
		int totalPrice = calculateTotalPrice(command);

		// 4. 임시 예약(PENDING) 생성
		Reservation reservation = reservationRepository.save(
			Reservation.builder()
				.roomTypeId(command.roomTypeId())
				.memberId(memberId)
				.checkIn(command.checkIn())
				.checkOut(command.checkOut())
				.guestCount(command.guestCount())
				.customerName(command.customerName())
				.phoneNumber(command.phoneNumber())
				.paymentMethod(command.paymentMethod())
				.totalPrice(totalPrice)
				.status(ReservationStatus.PENDING)
				.build()
		);

		// 5. 결제 URL 생성 (PG 연동 / Mock)
		String paymentUrl = generatePaymentUrl(reservation);

		return new CreateReservationResult(
			reservation.getId(),
			reservation.getStatus(),
			reservation.getTotalPrice(),
			paymentUrl
		);
	}

	private void validateAvailability(CreateReservationCommand command) {
		int requiredDayCount = (int)ChronoUnit.DAYS.between(command.checkIn(), command.checkOut());

		long count = roomAvailabilityRepository.countByRoomTypeIdAndOpenDateBetweenAndAvailableCountGreaterThanEqual(
			command.roomTypeId(), command.checkIn(), command.checkOut().minusDays(1), 1
		);

		if (count < requiredDayCount) {
			throw ErrorCode.CONFLICT.exception("선택한 날짜에는 객실 예약이 불가능합니다.");
		}
	}

	private int calculateTotalPrice(CreateReservationCommand command) {
		return roomAvailabilityRepository.sumPriceByRoomTypeIdAndDateRange(
			command.roomTypeId(), command.checkIn(), command.checkOut().minusDays(1)
		);
	}

	private String generatePaymentUrl(Reservation reservation) {
		// PG 연동 시 reservationId를 넘겨야 함
		return "https://mock-pg.com/pay?reservationId=" + reservation.getId();
	}

	private void decreaseAvailabilityCount(CreateReservationCommand command) {
		List<OriginRoomAvailability> availabilities =
			roomAvailabilityRepository.findAllByRoomTypeIdAndOpenDateBetween(
				command.roomTypeId(), command.checkIn(), command.checkOut().minusDays(1)
			);

		if (availabilities.size() != ChronoUnit.DAYS.between(command.checkIn(), command.checkOut())) {
			throw ErrorCode.CONFLICT.exception("예약 가능한 날짜 수량이 부족합니다.");
		}

		for (OriginRoomAvailability availability : availabilities) {
			availability.decreaseAvailableCount(); // 내부에서 availableCount-- 및 예외 처리
		}

		// 버전 기반 낙관적 락으로 saveAll 시점에 충돌 검증
		roomAvailabilityRepository.saveAll(availabilities);
	}
}

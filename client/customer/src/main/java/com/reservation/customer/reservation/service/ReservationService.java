package com.reservation.customer.reservation.service;

import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reservation.customer.accommodation.repository.JpaAccommodationRepository;
import com.reservation.customer.member.repository.JpaMemberRepository;
import com.reservation.customer.payment.repository.JpaPaymentRepository;
import com.reservation.customer.reservation.repository.JpaReservationRepository;
import com.reservation.customer.reservation.service.dto.CreateReservationCommand;
import com.reservation.customer.reservation.service.dto.CreateReservationResult;
import com.reservation.customer.roomavailability.repository.JpaRoomAvailabilityRepository;
import com.reservation.customer.roomtype.repository.JpaRoomTypeRepository;
import com.reservation.domain.member.Member;
import com.reservation.domain.reservation.Reservation;
import com.reservation.domain.reservation.enums.ReservationStatus;
import com.reservation.domain.roomavailability.OriginRoomAvailability;
import com.reservation.domain.roomtype.RoomType;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {
	private final JpaReservationRepository reservationRepository;
	private final JpaMemberRepository memberRepository;
	private final JpaRoomAvailabilityRepository roomAvailabilityRepository;
	private final JpaRoomTypeRepository roomTypeRepository;
	private final JpaAccommodationRepository accommodationRepository;
	private final JpaPaymentRepository paymentRepository;
	@Value("${iamport.imp-uid}")
	private String impUid;

	@Transactional
	public CreateReservationResult pessimisticCreateReservation(long memberId, CreateReservationCommand command) {
		// 0. 회원 존재 여부 확인
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("회원이 존재하지 않습니다."));

		// 1. 예약 룸 정보 조회
		RoomType roomType = roomTypeRepository.findById(command.roomTypeId())
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("룸 타입이 존재하지 않습니다."));
		// 2. 예약 가능 여부 확인
		validateAvailability(command);

		// 3. 수량 차감 (낙관적 락 기반)
		decreaseAvailabilityCount(command);

		// 4. 총 숙박 요금 계산
		int totalPrice = calculateTotalPrice(command);

		// 4. 임시 예약(PENDING) 생성
		Reservation reservation = reservationRepository.save(
			Reservation.builder()
				.roomTypeId(command.roomTypeId())
				.memberId(member.getId())
				.checkIn(command.checkIn())
				.checkOut(command.checkOut())
				.guestCount(command.guestCount())
				.phoneNumber(member.getPhoneNumber())
				.totalPrice(totalPrice)
				.status(ReservationStatus.PENDING)
				.build()
		);

		return new CreateReservationResult(
			reservation.getId(),
			member.getEmail(),
			member.getPhoneNumber(),
			roomType.getName(),
			reservation.getStatus(),
			reservation.getTotalPrice(),
			impUid
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

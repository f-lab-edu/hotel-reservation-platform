package com.reservation.customer.reservation.service;

import static com.reservation.support.utils.retry.OptimisticLockingFailureRetryUtils.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reservation.customer.member.repository.JpaMemberRepository;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
	private static final int MAX_OPTIMISTIC_RETRY_COUNT = 5;
	private static final long MAX_LOCK_WAIT_TIME_SECONDS = 10L; // 락 대기 최대 시간
	private static final long LOCK_WAIT_TIME_SECONDS = 5L;

	private final JpaReservationRepository reservationRepository;
	private final JpaMemberRepository memberRepository;
	private final JpaRoomAvailabilityRepository roomAvailabilityRepository;
	private final JpaRoomTypeRepository roomTypeRepository;
	private final RedissonClient redisson;

	@Value("${iamport.imp-uid}")
	private String impUid;

	/**
	 * 낙관적 락을 사용하여 예약 생성
	 * - 회원 존재 여부 확인
	 * - 룸 타입 존재 여부 확인
	 * - 예약 가능 여부 확인
	 * - 수량 차감 (낙관적 락 기반) 5회 retry
	 * - 총 숙박 요금 계산
	 * - 임시 예약(PENDING) 생성
	 */
	@Transactional
	public CreateReservationResult optimisticCreateReservation(long memberId, CreateReservationCommand command) {
		// 0. 회원 존재 여부 확인
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("회원이 존재하지 않습니다."));

		// 1. 예약 룸 정보 조회
		RoomType roomType = roomTypeRepository.findById(command.roomTypeId())
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("룸 타입이 존재하지 않습니다."));
		// 2. 예약 가능 여부 확인
		validateAvailability(command);

		// 3. 수량 차감 (낙관적 락 기반) 5회 retry
		executeWithRetry(MAX_OPTIMISTIC_RETRY_COUNT, () -> decreaseAvailabilityCount(command));

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

	private List<OriginRoomAvailability> decreaseAvailabilityCount(CreateReservationCommand command) {
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
		return roomAvailabilityRepository.saveAll(availabilities);
	}

	/**
	 * Redisson을 사용하여 예약 생성
	 * - 회원 존재 여부 확인
	 * - 룸 타입 존재 여부 확인
	 * - 예약 가능 여부 확인
	 * - 수량 차감 (Redisson Lock)
	 * - 총 숙박 요금 계산
	 * - 임시 예약(PENDING) 생성
	 */
	public CreateReservationResult redissonCreateReservation(long memberId, CreateReservationCommand command) {
		// 0. 회원 존재 여부 확인
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("회원이 존재하지 않습니다."));

		// 1. 예약 룸 정보 조회
		RoomType roomType = roomTypeRepository.findById(command.roomTypeId())
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("룸 타입이 존재하지 않습니다."));

		// 2. 비관적 락 처리 시작 (redisson 멀티 락 기반)
		RLock lock = getReservationPeriodLock(command);
		boolean isLocked = false;
		try {
			isLocked = lock.tryLock(MAX_LOCK_WAIT_TIME_SECONDS, LOCK_WAIT_TIME_SECONDS, TimeUnit.SECONDS);

			if (!isLocked) {
				throw ErrorCode.CONFLICT.exception("해당 객실은 현재 다른 예약 처리 중입니다. 잠시 후 다시 시도해 주세요.");
			}

			// 3. 예약 가능 여부 확인
			validateAvailability(command);

			// 4. 수량 차감
			decreaseAvailabilityCount(command);

			// 5. 총 숙박 요금 계산
			int totalPrice = calculateTotalPrice(command);

			// 6. 임시 예약(PENDING) 생성
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

		} catch (Exception e) {
			log.error(e.getMessage());
			throw ErrorCode.INTERNAL_SERVER_ERROR.exception("예약 처리 중 오류 발생");
		} finally {
			if (isLocked) {
				lock.unlock();
			}
		}
	}

	private RLock getReservationPeriodLock(CreateReservationCommand command) {
		List<RLock> locks = new ArrayList<>();

		for (LocalDate date = command.checkIn(); date.isBefore(command.checkOut()); date = date.plusDays(1)) {
			String key = "reservation:lock:" + command.roomTypeId() + ":" + date;
			locks.add(redisson.getLock(key));
		}

		return new RedissonMultiLock(locks.toArray(new RLock[0]));
	}
}

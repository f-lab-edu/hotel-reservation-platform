package com.reservation.customer.reservation.service;

import static com.reservation.support.utils.retry.OptimisticLockingFailureRetryUtils.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reservation.customer.member.repository.JpaMemberRepository;
import com.reservation.customer.payment.repository.JpaPaymentRepository;
import com.reservation.customer.payment.service.iamport.Iamport;
import com.reservation.customer.reservation.repository.JpaReservationRepository;
import com.reservation.customer.reservation.service.dto.CreateReservationCommand;
import com.reservation.customer.reservation.service.dto.CreateReservationResult;
import com.reservation.customer.reservation.service.dto.TemporaryReservation;
import com.reservation.customer.reservation.statemachine.ReservationStateMachineService;
import com.reservation.customer.roomavailability.repository.JpaRoomAvailabilityRepository;
import com.reservation.customer.roomavailabilitysummary.repository.JpaRoomAvailabilitySummaryRepository;
import com.reservation.customer.roomavailabilitysummary.repository.RoomAvailabilitySummaryRepository;
import com.reservation.customer.roomtype.repository.JpaRoomTypeRepository;
import com.reservation.domain.member.Member;
import com.reservation.domain.payment.Payment;
import com.reservation.domain.reservation.Reservation;
import com.reservation.domain.reservation.enums.ReservationEvents;
import com.reservation.domain.reservation.enums.ReservationStatus;
import com.reservation.domain.roomavailability.OriginRoomAvailability;
import com.reservation.domain.roomavailabilitysummary.RoomAvailabilitySummary;
import com.reservation.domain.roomtype.RoomType;
import com.reservation.support.exception.ErrorCode;
import com.siot.IamportRestClient.request.CancelData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
	private static final int MAX_OPTIMISTIC_RETRY_COUNT = 5;
	private static final long MAX_LOCK_WAIT_TIME_SECONDS = 10L; // 락 대기 최대 시간
	private static final long LOCK_WAIT_TIME_SECONDS = 5L;

	private static final int MAX_REDIS_RESERVATION_TTL_MINUTES = 10;

	private final Iamport iamport;

	private final ReservationStateMachineService stateMachineService;

	private final JpaMemberRepository memberRepository;
	private final JpaRoomAvailabilitySummaryRepository jpaAvailabilitySummaryRepository;
	private final RoomAvailabilitySummaryRepository availabilitySummaryRepository;
	private final JpaRoomTypeRepository roomTypeRepository;
	private final JpaReservationRepository reservationRepository;
	private final JpaRoomAvailabilityRepository roomAvailabilityRepository;
	private final JpaPaymentRepository paymentRepository;

	private final RedissonClient redisson;
	private final RedisReservationStore redisReservationStore;

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

		int requiredDayCount = (int)ChronoUnit.DAYS.between(command.checkIn(), command.checkOut());

		// 2. 예약 가능 여부 확인
		validateAvailability(command, requiredDayCount);

		// 3. 수량 차감 (낙관적 락 기반) 5회 retry
		RoomAvailabilitySummary availabilitySummary =
			executeWithRetry(MAX_OPTIMISTIC_RETRY_COUNT, () -> decreaseAvailabilityCount(command, requiredDayCount));

		// 4. 총 숙박 요금
		int totalPrice = availabilitySummary.getTotalPrice(requiredDayCount);

		// Reservation 저장 대신 Redis TTL 저장
		TemporaryReservation temp = new TemporaryReservation(
			memberId,
			command.roomTypeId(),
			command.checkIn(),
			command.checkOut(),
			command.guestCount(),
			member.getPhoneNumber(),
			totalPrice
		);

		redisReservationStore.save(temp, Duration.ofMinutes(MAX_REDIS_RESERVATION_TTL_MINUTES));
		log.info("임시 예약 저장 완료 - Redis key: {}", temp.redisKey());

		return new CreateReservationResult(
			member.getEmail(),
			member.getPhoneNumber(),
			roomType.getName(),
			totalPrice,
			impUid
		);
	}

	private void validateAvailability(CreateReservationCommand command, int requiredDayCount) {
		long count = availabilitySummaryRepository.findRemainAvailabilityCount(
			command.roomTypeId(), command.checkIn(), command.guestCount(), requiredDayCount
		);

		if (count <= 0) {
			throw ErrorCode.CONFLICT.exception("선택한 날짜에는 객실 예약이 불가능합니다.");
		}
	}

	private RoomAvailabilitySummary decreaseAvailabilityCount(CreateReservationCommand command, int requiredDayCount) {
		RoomAvailabilitySummary availabilitySummary =
			jpaAvailabilitySummaryRepository.findOneByRoomTypeIdAndCheckInDate(command.roomTypeId(), command.checkIn())
				.orElseThrow(() -> ErrorCode.CONFLICT.exception("예약 가능한 룸 정보가 없습니다."));

		availabilitySummary.decreaseAvailability(requiredDayCount);
		availabilitySummary.prePersist();

		return jpaAvailabilitySummaryRepository.save(availabilitySummary);
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

		// 혹시 모를 데드락을 줄이기 위해, 멀티락 X -> 순서대로 락을 획득하는 방식으로 변경
		// 2. 비관적 락 처리 시작 (redisson 락 기반)
		YearMonth checkInMonth = YearMonth.from(command.checkIn());
		YearMonth checkOutMonth = YearMonth.from(command.checkOut().minusDays(1));
		RLock checkInMonthLock = redisson.getLock("reservation:lock:" + command.roomTypeId() + ":" + checkInMonth);
		boolean isCheckInMonthLock = false;
		RLock checkOutMonthLock = redisson.getLock("reservation:lock:" + command.roomTypeId() + ":" + checkOutMonth);
		boolean isCheckOutMonthLock = false;
		try {
			isCheckInMonthLock =
				checkInMonthLock.tryLock(MAX_LOCK_WAIT_TIME_SECONDS, LOCK_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
			if (!isCheckInMonthLock) {
				throw ErrorCode.CONFLICT.exception("해당 객실은 현재 다른 예약 처리 중입니다. 잠시 후 다시 시도해 주세요.");
			}
			if (!checkInMonth.equals(checkOutMonth)) {
				isCheckOutMonthLock =
					checkOutMonthLock.tryLock(MAX_LOCK_WAIT_TIME_SECONDS, LOCK_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
				if (!isCheckOutMonthLock) {
					throw ErrorCode.CONFLICT.exception("해당 예약 건의 체크아웃 월은 현재 다른 결제 처리 중입니다. 잠시 후 다시 시도해 주세요.");
				}
			}

			// 3. 예약 가능 여부 확인
			validateAvailability(command);

			// 4. 수량 차감
			decreaseAvailabilityCount(command);

			// 5. 총 숙박 요금 계산
			int totalPrice = calculateTotalPrice(command);

			return new CreateReservationResult(
				member.getEmail(),
				member.getPhoneNumber(),
				roomType.getName(),
				totalPrice,
				impUid
			);

		} catch (Exception e) {
			log.error(e.getMessage());
			throw ErrorCode.INTERNAL_SERVER_ERROR.exception("예약 처리 중 오류 발생");
		} finally {
			if (isCheckOutMonthLock) {
				checkOutMonthLock.unlock();
			}
			if (isCheckInMonthLock) {
				checkInMonthLock.unlock();
			}
		}
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

	@Transactional
	public void cancelReservation(long memberId, Long reservationId) {
		// 1. 예약 조회
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("예약 정보를 찾을 수 없습니다."));

		// 2. 본인 예약인지 확인
		if (!reservation.getMemberId().equals(memberId)) {
			throw ErrorCode.FORBIDDEN.exception("본인의 예약만 취소할 수 있습니다.");
		}

		// 3. 결제 완료 상태인지 확인
		if (reservation.getStatus() != ReservationStatus.PAID) {
			throw ErrorCode.BAD_REQUEST.exception("결제 완료된 예약만 취소할 수 있습니다.");
		}

		String lockKey = "reservation:lock:" + reservation.getRoomTypeId() + ":" + reservation.getCheckIn();
		RLock lock = redisson.getLock(lockKey);
		boolean locked = false;

		try {
			locked = lock.tryLock(MAX_LOCK_WAIT_TIME_SECONDS, LOCK_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
			if (!locked) {
				throw ErrorCode.CONFLICT.exception("해당 예약 수량 복구 중 다른 요청이 처리 중입니다.");
			}

			// 4. 상태머신: 고객 취소 요청
			stateMachineService.sendEvent(reservation, ReservationEvents.CUSTOMER_PAYMENT_CANCEL);

			// 5. 아임포트 결제 취소
			Payment payment = paymentRepository.findByReservationId(reservationId)
				.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("결제 정보가 없습니다."));

			iamport.cancelPaymentByImpUid(
				new CancelData(payment.getPaymentUid(), true, new BigDecimal(payment.getPrice()))
			);
			payment.markCancelled();
			paymentRepository.save(payment);

			// 6. 상태머신: PG 결제 취소 성공 처리
			stateMachineService.sendEvent(reservation, ReservationEvents.PG_PAID_CANCEL);

			// 7. 예약 가능 수량 복구
			RoomAvailabilitySummary summary = jpaAvailabilitySummaryRepository
				.findOneByRoomTypeIdAndCheckInDate(reservation.getRoomTypeId(), reservation.getCheckIn())
				.orElseThrow(() -> ErrorCode.CONFLICT.exception("예약 가능 수량 정보가 없습니다."));

			int days = (int)ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());
			summary.increaseAvailability(days);
			summary.prePersist();
			jpaAvailabilitySummaryRepository.save(summary);

			// 8. 최종 예약 저장
			reservationRepository.save(reservation);

		} catch (Exception e) {
			log.error(e.getMessage());
			throw ErrorCode.CONFLICT.exception("예약 취소 처리 중 오류 발생");
		} finally {
			if (locked) {
				lock.unlock();
			}
		}
	}
}

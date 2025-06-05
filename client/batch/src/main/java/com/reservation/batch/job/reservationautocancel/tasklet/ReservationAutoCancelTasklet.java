package com.reservation.batch.job.reservationautocancel.tasklet;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.reservation.batch.repository.JpaReservationRepository;
import com.reservation.batch.repository.JpaRoomAvailabilitySummaryRepository;
import com.reservation.batch.repository.ReservationRepository;
import com.reservation.batch.repository.dto.Cursor;
import com.reservation.domain.reservation.Reservation;
import com.reservation.domain.roomavailabilitysummary.RoomAvailabilitySummary;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class ReservationAutoCancelTasklet implements Tasklet {
	private static final long MAX_LOCK_WAIT_TIME_SECONDS = 10L;
	private static final long LOCK_WAIT_TIME_SECONDS = 5L;

	private final RedissonClient redisson;

	private final JpaReservationRepository jpaReservationRepository;
	private final ReservationRepository reservationRepository;
	private final JpaRoomAvailabilitySummaryRepository jpaAvailabilitySummaryRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		Long lastSeenId = getLastSeenId(chunkContext.getAttribute("lastSeenId"));

		Cursor<Reservation, Long> cursor = reservationRepository.fetchOneNext(lastSeenId);
		if (cursor == null) {
			log.info("가계약 예약 최소 처리 존재하지 않음");
			return RepeatStatus.FINISHED;
		}

		try {
			processReservationWithLock(cursor.content());
		} catch (Exception e) {
			log.info("ReservationID({}) 가계약 예약 취소 처리 중 오류 발생: {}", cursor.content().getId(), e.getMessage());
		}

		return handleExecuteResult(cursor.hasNext(), cursor.nextCursor(), chunkContext);
	}

	private Long getLastSeenId(Object lastSeenId) {
		if (lastSeenId == null) {
			return null;
		}
		if (lastSeenId instanceof Long) {
			return (Long)lastSeenId;
		}
		throw ErrorCode.CONFLICT.exception("Invalid lastSeenId type: " + lastSeenId.getClass().getName());
	}

	@Transactional
	protected void processReservationWithLock(Reservation reservation) {
		// 혹시 모를 데드락을 줄이기 위해, 멀티락 X -> 순서대로 락을 획득하는 방식 채택
		RLock reservationLock = redisson.getLock("reservation:lock:" + reservation.getId());
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

			reservation.markCanceled();
			jpaReservationRepository.save(reservation);

			RoomAvailabilitySummary availabilitySummary =
				jpaAvailabilitySummaryRepository.findOneByRoomTypeIdAndCheckInDate(roomTypeId, reservation.getCheckIn())
					.orElseThrow(() -> ErrorCode.CONFLICT.exception("예약 가능한 룸 정보가 없습니다."));

			int requiredDayCount = (int)ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());

			availabilitySummary.increaseAvailability(requiredDayCount);
			availabilitySummary.prePersist();

			jpaAvailabilitySummaryRepository.save(availabilitySummary);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw ErrorCode.CONFLICT.exception(e.getMessage());
		} finally {
			if (isCheckInLock) {
				checkInLock.unlock();
			}
			if (isReservationLocked) {
				reservationLock.unlock();
			}
		}
	}

	private RepeatStatus handleExecuteResult(
		boolean hasNext,
		Long lastSeenId,
		ChunkContext chunkContext
	) {
		if (hasNext) {
			chunkContext.setAttribute("lastSeenId", lastSeenId);
			return RepeatStatus.CONTINUABLE;
		}
		return RepeatStatus.FINISHED;
	}
}

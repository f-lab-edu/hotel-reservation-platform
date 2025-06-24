package com.reservation.customer.reservation.statemachine.action;

import java.time.temporal.ChronoUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;

import com.reservation.customer.reservationstatushistory.repository.ReservationStatusHistoryRepository;
import com.reservation.customer.roomavailabilitysummary.repository.JpaRoomAvailabilitySummaryRepository;
import com.reservation.domain.reservation.Reservation;
import com.reservation.domain.reservation.enums.ReservationEvents;
import com.reservation.domain.reservation.enums.ReservationStatus;
import com.reservation.domain.reservationstatushistory.ReservationStatusHistory;
import com.reservation.domain.roomavailabilitysummary.RoomAvailabilitySummary;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReservationActionConfig {
	private final ReservationStatusHistoryRepository historyRepository;
	private final JpaRoomAvailabilitySummaryRepository jpaAvailabilitySummaryRepository;

	@Bean
	public Action<ReservationStatus, ReservationEvents> markPaidAction() {
		return context -> {
			Reservation reservation = (Reservation)context.getMessageHeader("reservation");
			if (reservation == null) {
				log.error("Reservation 객체가 Action에 전달되지 않았습니다.");
				return;
			}
			ReservationStatus from = reservation.getStatus();

			reservation.markPaid();
			log.info("예약 [{}] 상태를 'markPaid'로 전환합니다.", reservation.getId());

			ReservationStatus to = reservation.getStatus();

			historyRepository.save(ReservationStatusHistory.builder()
				.reservationId(reservation.getId())
				.fromStatus(from)
				.toStatus(to)
				.build());
		};
	}

	@Bean
	public Action<ReservationStatus, ReservationEvents> markPaidErrorAction() {
		return context -> {
			Reservation reservation = (Reservation)context.getMessageHeader("reservation");
			if (reservation == null) {
				log.error("Reservation 객체가 Action에 전달되지 않았습니다.");
				return;
			}
			ReservationStatus from = reservation.getStatus();

			reservation.markPaidError();
			log.info("예약 [{}] 상태를 'markPaidError'로 전환합니다.", reservation.getId());

			ReservationStatus to = reservation.getStatus();

			historyRepository.save(ReservationStatusHistory.builder()
				.reservationId(reservation.getId())
				.fromStatus(from)
				.toStatus(to)
				.build());
		};
	}

	@Bean
	public Action<ReservationStatus, ReservationEvents> markExpiredAction() {
		return context -> {
			Reservation reservation = (Reservation)context.getMessageHeader("reservation");
			if (reservation == null) {
				log.error("Reservation 객체가 Action에 전달되지 않았습니다.");
				return;
			}
			ReservationStatus from = reservation.getStatus();

			// reservation.markExpired();
			log.info("예약 [{}] 상태를 'markExpired'로 전환합니다.", reservation.getId());

			ReservationStatus to = reservation.getStatus();

			historyRepository.save(ReservationStatusHistory.builder()
				.reservationId(reservation.getId())
				.fromStatus(from)
				.toStatus(to)
				.build());
		};
	}

	@Bean
	public Action<ReservationStatus, ReservationEvents> markConfirmedAction() {
		return context -> {
			Reservation reservation = (Reservation)context.getMessageHeader("reservation");
			if (reservation == null) {
				log.error("Reservation 객체가 Action에 전달되지 않았습니다.");
				return;
			}
			ReservationStatus from = reservation.getStatus();

			reservation.markConfirmed();
			log.info("예약 [{}] 상태를 'markConfirmed'로 전환합니다.", reservation.getId());

			ReservationStatus to = reservation.getStatus();

			historyRepository.save(ReservationStatusHistory.builder()
				.reservationId(reservation.getId())
				.fromStatus(from)
				.toStatus(to)
				.build());
		};
	}

	@Bean
	public Action<ReservationStatus, ReservationEvents> markCanceledAction() {
		return context -> {
			Reservation reservation = (Reservation)context.getMessageHeader("reservation");
			if (reservation == null) {
				log.error("Reservation 객체가 Action에 전달되지 않았습니다.");
				return;
			}
			ReservationStatus from = reservation.getStatus();

			reservation.markCanceled();
			log.info("예약 [{}] 상태를 'markCanceled'로 전환합니다.", reservation.getId());

			ReservationStatus to = reservation.getStatus();

			historyRepository.save(ReservationStatusHistory.builder()
				.reservationId(reservation.getId())
				.fromStatus(from)
				.toStatus(to)
				.build());
		};
	}

	@Bean
	public Action<ReservationStatus, ReservationEvents> markPaidCanceledAction() {
		return context -> {
			Reservation reservation = (Reservation)context.getMessageHeader("reservation");
			if (reservation == null) {
				log.error("Reservation 객체가 Action에 전달되지 않았습니다.");
				return;
			}
			ReservationStatus from = reservation.getStatus();

			reservation.markPaidCanceled();
			log.info("예약 [{}] 상태를 'markPaidCanceled'로 전환합니다.", reservation.getId());

			ReservationStatus to = reservation.getStatus();

			historyRepository.save(ReservationStatusHistory.builder()
				.reservationId(reservation.getId())
				.fromStatus(from)
				.toStatus(to)
				.build());
		};
	}

	@Bean
	public Action<ReservationStatus, ReservationEvents> markPaidErrorCanceledAction() {
		return context -> {
			Reservation reservation = (Reservation)context.getMessageHeader("reservation");
			if (reservation == null) {
				log.error("Reservation 객체가 Action에 전달되지 않았습니다.");
				return;
			}
			ReservationStatus from = reservation.getStatus();

			reservation.markPaidErrorCanceled();
			log.info("예약 [{}] 상태를 'markPaidErrorCanceled'로 전환합니다.", reservation.getId());

			// ✅ 예약 가능 수량 원복
			RoomAvailabilitySummary summary = jpaAvailabilitySummaryRepository
				.findOneByRoomTypeIdAndCheckInDate(reservation.getRoomTypeId(), reservation.getCheckIn())
				.orElseThrow(() -> ErrorCode.CONFLICT.exception("예약 가능 수량이 없습니다."));

			int days = (int)ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());
			summary.increaseAvailability(days);
			summary.prePersist();
			jpaAvailabilitySummaryRepository.save(summary);

			ReservationStatus to = reservation.getStatus();

			historyRepository.save(ReservationStatusHistory.builder()
				.reservationId(reservation.getId())
				.fromStatus(from)
				.toStatus(to)
				.build());
		};
	}

	@Bean
	public Action<ReservationStatus, ReservationEvents> markPgCancelFailAction() {
		return context -> {
			Reservation reservation = (Reservation)context.getMessageHeader("reservation");
			if (reservation == null) {
				log.error("Reservation 객체가 Action에 전달되지 않았습니다.");
				return;
			}
			ReservationStatus from = reservation.getStatus();

			reservation.markPgCancelFail();
			log.info("예약 [{}] 상태를 'markPgCancelFail'로 전환합니다.", reservation.getId());

			ReservationStatus to = reservation.getStatus();

			historyRepository.save(ReservationStatusHistory.builder()
				.reservationId(reservation.getId())
				.fromStatus(from)
				.toStatus(to)
				.build());
		};
	}
}

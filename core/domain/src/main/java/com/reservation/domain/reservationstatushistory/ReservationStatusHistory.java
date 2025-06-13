package com.reservation.domain.reservationstatushistory;

import java.time.LocalDateTime;

import com.reservation.domain.base.BaseEntity;
import com.reservation.domain.reservation.enums.ReservationStatus;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ReservationStatusHistory extends BaseEntity {
	private Long reservationId;
	private ReservationStatus fromStatus;
	private ReservationStatus toStatus;
	private LocalDateTime transitionedAt;

	@Builder
	public ReservationStatusHistory(
		long reservationId,
		ReservationStatus fromStatus,
		ReservationStatus toStatus
	) {
		if (reservationId <= 0) {
			throw new IllegalArgumentException("reservationId는 필수입니다.");
		}
		if (fromStatus == null || toStatus == null) {
			throw new IllegalArgumentException("fromStatus와 toStatus는 필수입니다.");
		}

		this.reservationId = reservationId;
		this.fromStatus = fromStatus;
		this.toStatus = toStatus;
		this.transitionedAt = LocalDateTime.now();
	}
}

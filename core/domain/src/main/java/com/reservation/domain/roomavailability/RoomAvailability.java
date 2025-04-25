package com.reservation.domain.roomavailability;

import java.time.LocalDate;

import com.reservation.domain.base.BaseEntity;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
public class RoomAvailability extends BaseEntity {
	@Column(nullable = false)
	long roomId; // 룸 타입 ID

	@Column(nullable = false)
	LocalDate date; // 예약 가능 날짜

	@Column(nullable = false)
	Integer availableCount; // 예약 가능 개수

	protected RoomAvailability() {
	}

	@Builder
	public RoomAvailability(Long id, long roomId, LocalDate date, Integer availableCount) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("숙소 ID는 0보다 커야 합니다.");
		}
		if (roomId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 ID는 0보다 커야 합니다.");
		}
		if (date == null) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 날짜는 필수입니다.");
		}
		if (availableCount < 0) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 개수는 0 이상이어야 합니다.");
		}

		this.id = id;
		this.roomId = roomId;
		this.date = date;
		this.availableCount = availableCount;
	}
}

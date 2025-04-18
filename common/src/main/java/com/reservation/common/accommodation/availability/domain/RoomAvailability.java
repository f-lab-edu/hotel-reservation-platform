package com.reservation.common.accommodation.availability.domain;

import java.time.LocalDate;

import com.reservation.common.domain.BaseEntity;
import com.reservation.commonmodel.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;

@Getter
@Entity
public class RoomAvailability extends BaseEntity {
	@Column(nullable = false)
	Long roomTypeId; // 룸 타입 ID

	@Column(nullable = false)
	LocalDate date; // 예약 가능 날짜

	@Column(nullable = false)
	Integer availableCount; // 예약 가능 개수

	protected RoomAvailability() {
	}

	public RoomAvailability(Long id, Long roomTypeId, LocalDate date, Integer availableCount) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("숙소 ID는 0보다 커야 합니다.");
		}
		if (roomTypeId == null || roomTypeId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 타입 정보는 필수입니다.");
		}
		if (date == null) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 날짜는 필수입니다.");
		}
		if (availableCount < 0) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 개수는 0 이상이어야 합니다.");
		}

		this.id = id;
		this.roomTypeId = roomTypeId;
		this.date = date;
		this.availableCount = availableCount;
	}
}

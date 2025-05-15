package com.reservation.domain.roomavailability;

import java.io.Serializable;
import java.time.LocalDate;

import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RoomAvailabilityId implements Serializable {
	@Column(nullable = false)
	private long roomTypeId; // 룸 타입 ID
	@Column(nullable = false)
	private LocalDate openDate; // 예약 가능 날짜

	public RoomAvailabilityId(long roomTypeId, LocalDate openDate) {
		if (roomTypeId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 ID는 0보다 커야 합니다.");
		}
		if (openDate == null) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 날짜는 필수입니다.");
		}
		this.roomTypeId = roomTypeId;
		this.openDate = openDate;
	}
}

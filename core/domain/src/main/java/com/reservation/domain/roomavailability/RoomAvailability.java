package com.reservation.domain.roomavailability;

import java.time.LocalDate;

import com.reservation.domain.base.BaseEntity;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RoomAvailability extends BaseEntity {
	@Column(nullable = false)
	private long roomTypeId; // 룸 타입 ID

	@Column(nullable = false)
	private LocalDate date; // 예약 가능 날짜

	@Column(nullable = false)
	private Integer price; // 가격

	@Column(nullable = false)
	private Integer availableCount; // 예약 가능 개수

	@Builder
	public RoomAvailability(Long id, long roomTypeId, LocalDate date, int price, int availableCount) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("숙소 ID는 0보다 커야 합니다.");
		}
		if (roomTypeId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 ID는 0보다 커야 합니다.");
		}
		if (date == null) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 날짜는 필수입니다.");
		}
		if (price < 1000) {
			throw ErrorCode.BAD_REQUEST.exception("가격은 1000 이상이어야 합니다.");
		}
		if (availableCount < 0) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 개수는 0 이상이어야 합니다.");
		}

		this.id = id;
		this.roomTypeId = roomTypeId;
		this.date = date;
		this.price = price;
		this.availableCount = availableCount;
	}
}

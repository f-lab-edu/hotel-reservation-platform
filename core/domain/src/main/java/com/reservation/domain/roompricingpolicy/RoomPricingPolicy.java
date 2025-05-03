package com.reservation.domain.roompricingpolicy;

import java.time.DayOfWeek;

import com.reservation.domain.base.BaseEntity;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(
	name = "room_pricing_policy",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_room_price_day_of_week", columnNames = {"room_type_id", "day_of_week"})
	}
)
public class RoomPricingPolicy extends BaseEntity {
	@Column(nullable = false)
	private long roomId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private DayOfWeek dayOfWeek;

	@Column(nullable = false)
	private int price;

	protected RoomPricingPolicy() {
	}

	@Builder
	public RoomPricingPolicy(Long id, long roomId, DayOfWeek dayOfWeek, int price) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("ID는 0보다 커야 합니다.");
		}
		if (roomId <= 0) {
			throw ErrorCode.CONFLICT.exception("룸 ID는 0보다 커야 합니다.");
		}
		if (dayOfWeek == null) {
			throw ErrorCode.CONFLICT.exception("요일 정보는 필수입니다.");
		}
		if (price < 1000) {
			throw ErrorCode.CONFLICT.exception("가격은 1,000 이상이어야 합니다.");
		}
		this.id = id;
		this.roomId = roomId;
		this.dayOfWeek = dayOfWeek;
		this.price = price;
	}

	public void update(DayOfWeek dayOfWeek, int price) {
		if (dayOfWeek == null) {
			throw ErrorCode.CONFLICT.exception("요일 정보는 필수입니다.");
		}
		if (price < 1000) {
			throw ErrorCode.CONFLICT.exception("가격은 1,000 이상이어야 합니다.");
		}

		this.dayOfWeek = dayOfWeek;
		this.price = price;
	}
}

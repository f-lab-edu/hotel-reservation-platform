package com.reservation.domain.roomautoavailabilitypolicy;

import com.reservation.domain.base.BaseEntity;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
public class RoomAutoAvailabilityPolicy extends BaseEntity {
	@Column(nullable = false)
	private long roomId;

	@Column(nullable = false)
	private Boolean enabled; // 자동 생성 여부

	@Column(nullable = true)
	private Integer openDaysAheadOrNull; // 오늘부터 몇 일치 열어둘지

	@Column(nullable = true)
	private Integer maxRoomsPerDayOrNull; // 하루 최대 열어둘 방 수

	protected RoomAutoAvailabilityPolicy() {
	}

	@Builder
	public RoomAutoAvailabilityPolicy(
		Long id,
		long roomId,
		Boolean enabled,
		Integer openDaysAheadOrNull,
		Integer maxRoomsPerDayOrNull
	) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("숙소 ID는 0보다 커야 합니다.");
		}
		if (roomId <= 0) {
			throw ErrorCode.CONFLICT.exception("룸 ID는 0보다 커야 합니다.");
		}
		if (enabled == null) {
			throw ErrorCode.CONFLICT.exception("자동 생성 여부는 필수입니다.");
		}
		if (enabled && (openDaysAheadOrNull == null || openDaysAheadOrNull < 7)) {
			throw ErrorCode.CONFLICT.exception("자동 생성 예약 가능일은 7일 이상이어야 합니다.");
		}
		if (enabled && (maxRoomsPerDayOrNull == null || maxRoomsPerDayOrNull < 1)) {
			throw ErrorCode.CONFLICT.exception("자동 생성 예약 가능 개수는 1개 이상이어야 합니다.");
		}

		this.id = id;
		this.roomId = roomId;
		this.enabled = enabled;
		this.openDaysAheadOrNull = openDaysAheadOrNull;
		this.maxRoomsPerDayOrNull = maxRoomsPerDayOrNull;
	}

	public void update(
		boolean enabled,
		Integer openDaysAheadOrNull,
		Integer maxRoomsPerDayOrNull
	) {
		if (enabled && (openDaysAheadOrNull == null || openDaysAheadOrNull < 7)) {
			throw ErrorCode.CONFLICT.exception("자동 생성 예약 가능일은 7일 이상이어야 합니다.");
		}
		if (enabled && (maxRoomsPerDayOrNull == null || maxRoomsPerDayOrNull < 1)) {
			throw ErrorCode.CONFLICT.exception("자동 생성 예약 가능 개수는 1개 이상이어야 합니다.");
		}
		this.enabled = enabled;
		this.openDaysAheadOrNull = openDaysAheadOrNull;
		this.maxRoomsPerDayOrNull = maxRoomsPerDayOrNull;

	}
}

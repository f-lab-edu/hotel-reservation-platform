package com.reservation.domain.roomautoavailabilitypolicy;

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
public class RoomAutoAvailabilityPolicy extends BaseEntity {
	@Column(nullable = false)
	private long roomTypeId;

	@Column(nullable = false)
	private Boolean enabled; // 자동 생성 여부

	@Column(nullable = true)
	private Integer openDaysAhead; // 오늘부터 몇 일치 열어둘지

	@Column(nullable = true)
	private Integer maxRoomsPerDay; // 하루 최대 열어둘 방 수

	@Builder
	public RoomAutoAvailabilityPolicy(
		Long id,
		long roomTypeId,
		boolean enabled,
		int openDaysAhead,
		int maxRoomsPerDay
	) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("숙소 ID는 0보다 커야 합니다.");
		}
		if (roomTypeId <= 0) {
			throw ErrorCode.CONFLICT.exception("룸 ID는 0보다 커야 합니다.");
		}
		if (!enabled && (openDaysAhead > 0 || maxRoomsPerDay > 0)) {
			throw ErrorCode.CONFLICT.exception("자동 생성 여부가 false일 때는 예약 가능일과 개수를 설정할 수 없습니다.");
		}
		if (enabled && openDaysAhead < 7) {
			throw ErrorCode.CONFLICT.exception("자동 생성 예약 가능일은 7일 이상이어야 합니다.");
		}
		if (enabled && maxRoomsPerDay < 1) {
			throw ErrorCode.CONFLICT.exception("자동 생성 예약 가능 개수는 1개 이상이어야 합니다.");
		}

		this.id = id;
		this.roomTypeId = roomTypeId;
		this.enabled = enabled;
		this.openDaysAhead = openDaysAhead;
		this.maxRoomsPerDay = maxRoomsPerDay;
	}

	public void update(
		boolean enabled,
		int openDaysAhead,
		int maxRoomsPerDay
	) {
		if (!enabled && (openDaysAhead > 0 || maxRoomsPerDay > 0)) {
			throw ErrorCode.CONFLICT.exception("자동 생성 여부가 false일 때는 예약 가능일과 개수를 설정할 수 없습니다.");
		}
		if (enabled && openDaysAhead < 7) {
			throw ErrorCode.CONFLICT.exception("자동 생성 예약 가능일은 7일 이상이어야 합니다.");
		}
		if (enabled && maxRoomsPerDay < 1) {
			throw ErrorCode.CONFLICT.exception("자동 생성 예약 가능 개수는 1개 이상이어야 합니다.");
		}
		this.enabled = enabled;
		this.openDaysAhead = openDaysAhead;
		this.maxRoomsPerDay = maxRoomsPerDay;

	}
}

package com.reservation.host.roomautoavailabilitypolicy.controller.request;

import com.reservation.host.roomautoavailabilitypolicy.service.dto.DefaultRoomAutoAvailabilityPolicyInfo;
import com.reservation.support.exception.ErrorCode;

public record NewRoomAutoAvailabilityPolicyRequest(
	boolean enabled,
	int openDaysAhead,
	int maxRoomsPerDay
) {
	public DefaultRoomAutoAvailabilityPolicyInfo validateToDefaultRoomAutoAvailabilityPolicyInfo() {
		if (!enabled && (openDaysAhead > 0 || maxRoomsPerDay > 0)) {
			throw ErrorCode.BAD_REQUEST.exception("자동 생성 여부가 false일 때는 예약 가능일과 개수를 설정할 수 없습니다.");
		}
		if (enabled && openDaysAhead < 7) {
			throw ErrorCode.BAD_REQUEST.exception("자동 생성 예약 가능일은 7일 이상이어야 합니다.");
		}
		if (enabled && maxRoomsPerDay < 1) {
			throw ErrorCode.BAD_REQUEST.exception("자동 생성 예약 가능 개수는 1개 이상이어야 합니다.");
		}
		return new DefaultRoomAutoAvailabilityPolicyInfo(
			enabled,
			openDaysAhead,
			maxRoomsPerDay
		);
	}
}

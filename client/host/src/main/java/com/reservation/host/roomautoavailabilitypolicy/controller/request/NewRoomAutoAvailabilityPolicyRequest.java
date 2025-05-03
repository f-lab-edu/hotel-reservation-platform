package com.reservation.host.roomautoavailabilitypolicy.controller.request;

import com.reservation.host.roomautoavailabilitypolicy.service.dto.DefaultRoomAutoAvailabilityPolicyInfo;
import com.reservation.support.exception.ErrorCode;

public record NewRoomAutoAvailabilityPolicyRequest(
	Boolean enabled,
	Integer openDaysAheadOrNull,
	Integer maxRoomsPerDayOrNull
) {
	public DefaultRoomAutoAvailabilityPolicyInfo validateToDefaultRoomAutoAvailabilityPolicyInfo() {
		if (enabled == null) {
			throw ErrorCode.BAD_REQUEST.exception("자동 생성 여부는 필수입니다.");
		}
		if (enabled && (openDaysAheadOrNull == null || openDaysAheadOrNull < 7 || openDaysAheadOrNull > 90)) {
			throw ErrorCode.BAD_REQUEST.exception("자동 생성 예약 가능일은 7일 이상 90일 이하여야 합니다.");
		}
		if (enabled && (maxRoomsPerDayOrNull == null || maxRoomsPerDayOrNull < 0)) {
			throw ErrorCode.BAD_REQUEST.exception("자동 생성 예약 가능 개수는 0개 이상이어야 합니다.");
		}
		if (!enabled) {
			return new DefaultRoomAutoAvailabilityPolicyInfo(
				false,
				null,
				null
			);
		}

		return new DefaultRoomAutoAvailabilityPolicyInfo(
			true,
			openDaysAheadOrNull,
			maxRoomsPerDayOrNull
		);
	}
}

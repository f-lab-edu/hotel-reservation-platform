package com.reservation.host.roomautoavailabilitypolicy.service.dto;

public record DefaultRoomAutoAvailabilityPolicyInfo(
	boolean enabled,
	Integer openDaysAheadOrNull,
	Integer maxRoomsPerDayOrNull
) {
}

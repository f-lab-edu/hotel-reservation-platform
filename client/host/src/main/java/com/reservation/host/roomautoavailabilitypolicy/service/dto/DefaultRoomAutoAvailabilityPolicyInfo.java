package com.reservation.host.roomautoavailabilitypolicy.service.dto;

public record DefaultRoomAutoAvailabilityPolicyInfo(
	boolean enabled,
	int openDaysAhead,
	int maxRoomsPerDay
) {
}

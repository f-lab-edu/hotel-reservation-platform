package com.reservation.host.roompricingpolicy.service.dto;

import java.time.DayOfWeek;

public record DefaultRoomPricingPolicyInfo(
	DayOfWeek dayOfWeek,
	int price
) {
}

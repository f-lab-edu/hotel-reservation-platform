package com.reservation.host.roomavailability.service.dto;

import java.time.LocalDate;

public record DefaultRoomAvailabilityInfo(
	long roomId,
	LocalDate date,
	int price,
	int availableCount
) {
}

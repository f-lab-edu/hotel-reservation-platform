package com.reservation.host.accommodation.availability.controller.dto.request;

import java.time.LocalDate;

public record CreateRoomAvailabilityRequest(
	Long roomTypeId,
	LocalDate date,
	int availableCount
) {
}

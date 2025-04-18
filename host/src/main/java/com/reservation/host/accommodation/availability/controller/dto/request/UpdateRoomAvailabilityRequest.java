package com.reservation.host.accommodation.availability.controller.dto.request;

import java.time.LocalDate;

public record UpdateRoomAvailabilityRequest(
	Long id,
	Long roomTypeId,
	LocalDate date,
	int availableCount
) {
}

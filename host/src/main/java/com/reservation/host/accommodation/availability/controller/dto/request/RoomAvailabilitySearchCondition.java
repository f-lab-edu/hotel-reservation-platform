package com.reservation.host.accommodation.availability.controller.dto.request;

import java.time.LocalDate;

public record RoomAvailabilitySearchCondition(
	Long roomTypeId,
	LocalDate startDate,
	LocalDate endDate
) {
}

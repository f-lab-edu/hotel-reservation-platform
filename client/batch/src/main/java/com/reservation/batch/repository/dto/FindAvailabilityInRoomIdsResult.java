package com.reservation.batch.repository.dto;

import java.time.LocalDate;

public record FindAvailabilityInRoomIdsResult(
	long roomId,
	LocalDate date
) {
}

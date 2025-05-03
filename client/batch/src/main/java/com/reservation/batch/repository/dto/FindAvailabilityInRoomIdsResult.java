package com.reservation.batch.repository.dto;

import java.time.LocalDate;

public record FindAvailabilityInRoomIdsResult(
	long roomTypeId,
	LocalDate date
) {
	public FindAvailabilityInRoomIdsResult(long roomTypeId, LocalDate date) {
		this.roomTypeId = roomTypeId;
		this.date = date;
	}
}

package com.reservation.batch.repository.dto;

import java.time.LocalDate;

public record FindAvailabilityInRoomIdsResult(
	long roomId,
	LocalDate date
) {
	public FindAvailabilityInRoomIdsResult(long roomId, LocalDate date) {
		this.roomId = roomId;
		this.date = date;
	}
}

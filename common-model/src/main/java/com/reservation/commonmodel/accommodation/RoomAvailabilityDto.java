package com.reservation.commonmodel.accommodation;

import java.time.LocalDate;

public record RoomAvailabilityDto(
	Long id,
	Long roomTypeId,
	LocalDate date,
	Integer availableCount // 예약 가능 개수
) {
}

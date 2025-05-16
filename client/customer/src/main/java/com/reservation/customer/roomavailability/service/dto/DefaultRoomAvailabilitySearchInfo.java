package com.reservation.customer.roomavailability.service.dto;

import java.time.LocalDate;

public record DefaultRoomAvailabilitySearchInfo(
	LocalDate checkIn,
	LocalDate checkOut,
	Integer capacity
) {
}

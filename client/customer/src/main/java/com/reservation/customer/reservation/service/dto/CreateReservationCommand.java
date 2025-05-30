package com.reservation.customer.reservation.service.dto;

import java.time.LocalDate;

public record CreateReservationCommand(
	Long roomTypeId,
	LocalDate checkIn,
	LocalDate checkOut,
	int guestCount
) {
}

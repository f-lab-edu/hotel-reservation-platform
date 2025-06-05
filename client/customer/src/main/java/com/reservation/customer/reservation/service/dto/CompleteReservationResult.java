package com.reservation.customer.reservation.service.dto;

import java.time.LocalDate;

import com.reservation.domain.reservation.enums.ReservationStatus;

public record CompleteReservationResult(
	Long reservationId,
	ReservationStatus status,
	String accommodationName,
	String roomTypeName,
	LocalDate checkIn,
	LocalDate checkOut,
	int totalPrice
) {
}

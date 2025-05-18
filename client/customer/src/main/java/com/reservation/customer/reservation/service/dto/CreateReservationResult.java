package com.reservation.customer.reservation.service.dto;

import com.reservation.domain.reservation.enums.ReservationStatus;

public record CreateReservationResult(
	Long reservationId,
	ReservationStatus status,
	int totalPrice,
	String paymentUrl
) {
}

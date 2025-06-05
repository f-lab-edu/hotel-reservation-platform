package com.reservation.customer.reservation.service.dto;

import com.reservation.domain.reservation.enums.ReservationStatus;

public record CreateReservationResult(
	Long reservationId,
	String memberEmail,
	String memberPhoneNumber,
	String roomTypeName,
	ReservationStatus status,
	int totalPrice,
	String impUid
) {
}

package com.reservation.customer.reservation.service.dto;

public record CreateReservationResult(
	String memberEmail,
	String memberPhoneNumber,
	String roomTypeName,
	int totalPrice,
	String impUid
) {
}

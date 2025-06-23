package com.reservation.customer.payment.service.dto;

import java.time.LocalDate;

public record PaymentCheckCommand(
	long memberId,
	String paymentUid,
	long roomTypeId,
	LocalDate checkIn,
	LocalDate checkOut,
	String phoneNumber,
	int guestsCount
) {
}

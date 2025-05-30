package com.reservation.customer.payment.controller.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentCheckRequest(
	@NotNull @NotEmpty
	String paymentUid,
	@NotNull @Positive
	Long reservationId
) {
}

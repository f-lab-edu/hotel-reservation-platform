package com.reservation.customer.phoneverification.controller.dto.request;

import java.util.List;

import jakarta.annotation.Nonnull;

public record PhoneVerificationRequest(
	@Nonnull
	String phoneNumber,
	@Nonnull
	List<Long> agreedTermsIds
) {
}

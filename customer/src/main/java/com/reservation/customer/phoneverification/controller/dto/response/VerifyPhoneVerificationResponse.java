package com.reservation.customer.phoneverification.controller.dto.response;

import java.util.Set;

public record VerifyPhoneVerificationResponse(
	Set<Long> agreedTermsIds
) {
}

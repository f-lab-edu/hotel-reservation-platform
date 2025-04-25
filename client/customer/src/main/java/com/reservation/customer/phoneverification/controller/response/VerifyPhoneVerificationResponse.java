package com.reservation.customer.phoneverification.controller.response;

import java.util.List;

public record VerifyPhoneVerificationResponse(
	List<Long> agreedTermsIds
) {
}

package com.reservation.customer.phoneverification.service.dto;

import java.util.Set;

public record PhoneVerificationRedisValue(
	String verificationCode,
	Set<Long> agreedTermsIds
) {
}

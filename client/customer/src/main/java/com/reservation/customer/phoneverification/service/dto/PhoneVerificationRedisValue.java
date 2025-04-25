package com.reservation.customer.phoneverification.service.dto;

import java.util.List;

public record PhoneVerificationRedisValue(
	String verificationCode,
	List<Long> agreedTermsIds
) {
}

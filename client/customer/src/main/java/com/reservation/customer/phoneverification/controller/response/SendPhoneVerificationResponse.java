package com.reservation.customer.phoneverification.controller.response;

import java.time.LocalDateTime;

public record SendPhoneVerificationResponse(
	LocalDateTime expiresAt
) {
}

package com.reservation.customer.phoneverification.controller.dto.response;

import java.time.LocalDateTime;

public record SendPhoneVerificationResponse(
	LocalDateTime expiresAt
) {
}

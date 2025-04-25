package com.reservation.customer.phoneverification.service;

import java.time.LocalDateTime;

public interface VerificationExpiresTimeGeneration {
	LocalDateTime createExpiresTime();
}

package com.reservation.customer.phoneverification.infra;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PhoneVerificationExpiresTimeGeneratorTest {

	@Test
	@DisplayName("인증번호 만료 시간 생성 테스트")
	void createExpiresTime_ReturnsFutureTime() {
		LocalDateTime expiresTime = PhoneVerificationExpiresTimeGenerator.createExpiresTime();

		assertThat(expiresTime).isAfter(LocalDateTime.now());
		assertThat(expiresTime).isBefore(LocalDateTime.now().plusMinutes(4));
	}
}

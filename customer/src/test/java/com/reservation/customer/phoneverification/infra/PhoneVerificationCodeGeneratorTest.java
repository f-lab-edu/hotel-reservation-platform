package com.reservation.customer.phoneverification.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PhoneVerificationCodeGeneratorTest {

	@Test
	@DisplayName("인증번호 생성 테스트")
	void createCode_ReturnsFourDigitCode() {
		String code = PhoneVerificationCodeGenerator.createCode();

		assertThat(code).hasSize(4);
		assertThat(code).matches("\\d{4}");
	}
}

package com.reservation.customer.phoneverification.infra;

import java.time.LocalDateTime;

public class PhoneVerificationExpiresTimeGenerator {
	private final static int EXPIRES_TIME_MINUTES = 3;

	/**
	 * 인증번호 만료시간을 생성합니다.
	 * @return 인증번호 만료시간
	 */
	public static LocalDateTime createExpiresTime() {
		return LocalDateTime.now().plusMinutes(EXPIRES_TIME_MINUTES);
	}

}

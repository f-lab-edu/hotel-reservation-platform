package com.reservation.customer.phoneverification.infra;

import java.util.Random;

public class PhoneVerificationCodeGenerator {
	private static final int RANDOM_BOUND = 10000;

	/**
	 * 인증번호를 생성합니다.
	 * @return 인증번호
	 */
	public static String createCode() {
		return String.format("%04d", new Random().nextInt(RANDOM_BOUND));
	}
}

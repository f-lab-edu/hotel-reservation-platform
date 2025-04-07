package com.reservation.customer.phoneverification.infra;

import java.util.Random;

public class PhoneVerificationCodeGenerator {
	public static String createCode() {
		return String.format("%04d", new Random().nextInt(10000));
	}
}

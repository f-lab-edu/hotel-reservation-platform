package com.reservation.customer.phoneverification.controller.request;

import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendPhoneVerificationRequest(
	@NotBlank
	@Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
	String phoneNumber,
	@Nonnull
	List<Long> agreedTermsIds
) {
}

package com.reservation.customer.member.controller.dto.request;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
	@Nonnull @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
	String phoneNumber,
	@Nonnull @Email
	String email,
	@NotBlank
	String password) {
}

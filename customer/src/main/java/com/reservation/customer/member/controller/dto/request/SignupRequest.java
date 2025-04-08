package com.reservation.customer.member.controller.dto.request;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
	@NotBlank @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
	String phoneNumber,
	@NotBlank @Email
	String email,
	@NotBlank @Length(min = 8, max = 30)
	String password) {
}

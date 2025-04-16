package com.reservation.host.auth.controller.dto.request;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
	@NotBlank @Email
	String email,
	@NotBlank @Length(min = 8, max = 30)
	String password) {
}

package com.reservation.customer.auth.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
	@NotNull @Email
	String email,
	@NotNull @NotBlank
	String password) {
}

package com.reservation.admin.terms.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminUpdateClauseRequest(
	@NotNull @Min(1L) Long id,
	@Min(1) Integer clauseOrder,
	@NotBlank String title,
	@NotBlank String content
) {
}

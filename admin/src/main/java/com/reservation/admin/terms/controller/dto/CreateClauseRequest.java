package com.reservation.admin.terms.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateClauseRequest(
	@Min(1) Integer clauseOrder,
	@NotBlank String title,
	@NotBlank String content
) {
}

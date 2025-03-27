package com.reservation.admin.terms.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AdminCreateTermsRequest(
	@NotNull String code,
	@NotBlank String title,
	@NotNull String type,
	@NotNull String status,
	@NotNull LocalDateTime exposedFrom,
	@Future LocalDateTime exposedTo,
	@Min(1) Integer displayOrder,
	@NotEmpty List<@Valid AdminCreateClauseRequest> clauses
) {
}

package com.reservation.admin.terms.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateTermsRequest(
	@NotNull @Min(1L) Long id,
	@NotNull TermsCode code,
	@NotBlank String title,
	@NotNull TermsType type,
	@NotNull TermsStatus status,
	@NotNull LocalDateTime exposedFrom,
	@Future LocalDateTime exposedTo,
	@Min(1) Integer displayOrder,
	@NotEmpty List<@Valid UpdateClauseRequest> clauses
) {
}

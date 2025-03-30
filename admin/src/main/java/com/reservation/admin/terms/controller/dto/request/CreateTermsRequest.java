package com.reservation.admin.terms.controller.dto.request;

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

public record CreateTermsRequest(
	@NotNull TermsCode code,
	@NotBlank String title,
	@NotNull TermsType type,
	@NotNull TermsStatus status,
	@NotNull LocalDateTime exposedFrom,
	@Future LocalDateTime exposedTo,
	@Min(1) Integer displayOrder,
	@NotEmpty List<@Valid CreateClauseRequest> clauses
) {
}

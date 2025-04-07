package com.reservation.customer.terms.controller.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.reservation.commonmodel.terms.ClauseDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

public record TermsDetailResponse(
	Long id,
	TermsCode code,
	String title,
	TermsType type,
	TermsStatus status,
	Integer version,
	LocalDateTime exposedFrom,
	Integer displayOrder,
	List<ClauseDto> clauses
) {
}

package com.reservation.admin.terms.controller.dto.response;

import java.time.LocalDateTime;

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
	Boolean isLatest,
	LocalDateTime exposedFrom,
	LocalDateTime exposedToOrNull,
	Integer displayOrder,
	LocalDateTime createdAt
) {
}

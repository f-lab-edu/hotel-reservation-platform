package com.reservation.customer.terms.service.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;
import com.reservation.domain.terms.enums.TermsCode;
import com.reservation.domain.terms.enums.TermsStatus;
import com.reservation.domain.terms.enums.TermsType;

import lombok.Getter;

@Getter
public class SearchTerms {
	Long id;
	TermsCode code;
	String title;
	TermsType type;
	TermsStatus status;
	Integer version;
	Boolean isLatest;
	LocalDateTime exposedFrom;
	LocalDateTime exposedToOrNull;
	Integer displayOrder;
	LocalDateTime createdAt;

	@QueryProjection
	public SearchTerms(
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
		this.id = id;
		this.code = code;
		this.title = title;
		this.type = type;
		this.status = status;
		this.version = version;
		this.isLatest = isLatest;
		this.exposedFrom = exposedFrom;
		this.exposedToOrNull = exposedToOrNull;
		this.displayOrder = displayOrder;
		this.createdAt = createdAt;
	}
}

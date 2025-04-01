package com.reservation.commonapi.terms.repository.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

import lombok.Getter;

@Getter
public class AdminTermsDto {
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
	public AdminTermsDto(
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

package com.reservation.commonapi.customer.repository.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

import lombok.Getter;

@Getter
public class CustomerTermsDto {
	Long id;
	TermsCode code;
	String title;
	TermsType type;
	TermsStatus status;
	Integer version;
	LocalDateTime exposedFrom;
	Integer displayOrder;

	@QueryProjection
	public CustomerTermsDto(
		Long id,
		TermsCode code,
		String title,
		TermsType type,
		TermsStatus status,
		Integer version,
		LocalDateTime exposedFrom,
		Integer displayOrder
	) {
		this.id = id;
		this.code = code;
		this.title = title;
		this.type = type;
		this.status = status;
		this.version = version;
		this.exposedFrom = exposedFrom;
		this.displayOrder = displayOrder;
	}
}


package com.reservation.commonapi.terms.repository.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

public record AdminTermsDto(
	Long id,
	TermsCode code,
	String title,
	TermsType type,
	TermsStatus status,
	Integer version,
	LocalDateTime exposedFrom,
	LocalDateTime exposedTo,
	Integer displayOrder,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	List<AdminClauseDto> clauses
) {

}

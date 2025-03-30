package com.reservation.commonmodel.terms;

import java.time.LocalDateTime;
import java.util.List;

public record TermsDto(
	Long id,
	TermsCode code,
	String title,
	TermsType type,
	TermsStatus status,
	Integer version,
	Boolean isLatest,
	LocalDateTime exposedFrom,
	LocalDateTime exposedTo,
	Integer displayOrder,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	List<ClauseDto> clauses
) {

}

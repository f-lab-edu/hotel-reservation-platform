package com.reservation.commonapi.terms.repository.dto;

public record AdminClauseDto(
	Long id,
	Integer clauseOrder,
	String title,
	String content
) {
}

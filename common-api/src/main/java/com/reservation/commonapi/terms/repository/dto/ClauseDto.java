package com.reservation.commonapi.terms.repository.dto;

public record ClauseDto(
	Long id,
	Integer clauseOrder,
	String title,
	String content
) {
}

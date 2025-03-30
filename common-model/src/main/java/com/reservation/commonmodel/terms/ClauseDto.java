package com.reservation.commonmodel.terms;

public record ClauseDto(
	Long id,
	Integer clauseOrder,
	String title,
	String content
) {
}

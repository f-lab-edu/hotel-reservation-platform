package com.reservation.admin.terms.controller.request;

import com.reservation.domain.terms.Clause;
import com.reservation.domain.terms.Terms;

public record UpdateClauseRequest(
	Integer clauseOrder,
	String title,
	String content
) {
	public Clause validToClause(Terms updateTerms) {
		if (clauseOrder == null || clauseOrder <= 0) {
			throw new IllegalArgumentException("조항 순서는 0보다 커야 합니다.");
		}
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("조항 제목은 필수입니다.");
		}
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("조항 내용은 필수입니다.");
		}
		if (updateTerms == null) {
			throw new IllegalArgumentException("약관이 필요합니다.");
		}

		return Clause.builder()
			.terms(updateTerms)
			.clauseOrder(clauseOrder)
			.title(title)
			.content(content)
			.build();
	}
}

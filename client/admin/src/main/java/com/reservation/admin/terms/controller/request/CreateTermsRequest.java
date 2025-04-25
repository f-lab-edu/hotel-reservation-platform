package com.reservation.admin.terms.controller.request;

import java.time.LocalDateTime;
import java.util.List;

import com.reservation.domain.terms.Clause;
import com.reservation.domain.terms.Terms;
import com.reservation.domain.terms.enums.TermsCode;
import com.reservation.domain.terms.enums.TermsStatus;
import com.reservation.domain.terms.enums.TermsType;
import com.reservation.support.exception.ErrorCode;

public record CreateTermsRequest(
	TermsCode code,
	String title,
	TermsType type,
	LocalDateTime exposedFrom,
	LocalDateTime exposedToOrNull,
	Integer displayOrder,
	List<CreateClauseRequest> clauses
) {
	public Terms validToTerms() {
		if (code == null) {
			throw ErrorCode.BAD_REQUEST.exception("약관 코드가 필요합니다.");
		}
		if (title == null || title.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("약관 제목이 필요합니다.");
		}
		if (type == null) {
			throw ErrorCode.BAD_REQUEST.exception("약관 타입이 필요합니다.");
		}
		if (exposedFrom == null) {
			throw ErrorCode.BAD_REQUEST.exception("약관 노출 시작일이 필요합니다.");
		}
		if (displayOrder == null || displayOrder < 0) {
			throw ErrorCode.BAD_REQUEST.exception("약관 노출 순서는 0 이상이어야 합니다.");
		}
		if (clauses == null || clauses.isEmpty()) {
			throw ErrorCode.BAD_REQUEST.exception("약관 조항이 필요합니다.");
		}

		Terms newTerms = Terms.builder()
			.code(code)
			.title(title)
			.type(type)
			.status(TermsStatus.ACTIVE)
			.exposedFrom(exposedFrom)
			.displayOrder(displayOrder)
			.build();

		List<Clause> clauses = this.clauses.stream().map(request -> request.validToClause(newTerms)).toList();

		newTerms.setClauses(clauses);
		return newTerms;
	}
}

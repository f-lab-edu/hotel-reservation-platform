package com.reservation.common.terms.repository.mapper;

import java.util.List;

import com.reservation.common.clause.domain.Clause;
import com.reservation.common.terms.domain.Clauses;
import com.reservation.common.terms.domain.Terms;
import com.reservation.commonmodel.terms.ClauseDto;
import com.reservation.commonmodel.terms.TermsDto;

public class TermsDtoMapper {
	public static TermsDto fromTerms(Terms terms) {
		List<ClauseDto> adminClauses = terms.getClauses()
			.stream()
			.map(c -> new ClauseDto(c.getId(), c.getClauseOrder(), c.getTitle(), c.getContent()))
			.toList();

		return new TermsDto(
			terms.getId(),
			terms.getCode(),
			terms.getTitle(),
			terms.getType(),
			terms.getStatus(),
			terms.getVersion(),
			terms.getIsLatest(),
			terms.getExposedFrom(),
			terms.getExposedToOrNull(),
			terms.getDisplayOrder(),
			terms.getCreatedAt(),
			terms.getUpdatedAt(),
			adminClauses);
	}

	public static Terms ToTerms(TermsDto termsDto) {
		Terms terms = new Terms.TermsBuilder()
			.code(termsDto.code())
			.title(termsDto.title())
			.type(termsDto.type())
			.status(termsDto.status())
			.version(termsDto.version())
			.isLatest(termsDto.isLatest())
			.exposedFrom(termsDto.exposedFrom())
			.exposedToOrNull(termsDto.exposedToOrNull())
			.displayOrder(termsDto.displayOrder())
			.build();

		List<Clause> clauses = termsDto.clauses().stream()
			.map(dto -> new Clause.ClauseBuilder()
				.terms(terms)
				.clauseOrder(dto.clauseOrder())
				.title(dto.title())
				.content(dto.content())
				.build())
			.toList();

		terms.setClauses(new Clauses(clauses));

		return terms;
	}
}

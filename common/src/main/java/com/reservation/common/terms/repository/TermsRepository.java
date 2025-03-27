package com.reservation.common.terms.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.reservation.common.clause.domain.Clause;
import com.reservation.common.clause.domain.Clause.ClauseBuilder;
import com.reservation.common.terms.domain.Clauses;
import com.reservation.common.terms.domain.Terms;
import com.reservation.common.terms.domain.Terms.TermsBuilder;
import com.reservation.commonapi.terms.repository.AdminTermsRepository;
import com.reservation.commonapi.terms.repository.dto.AdminTermsDto;

@Repository
public class TermsRepository implements AdminTermsRepository {

	private final JpaTermsRepository jpaTermsRepository;

	public TermsRepository(JpaTermsRepository jpaTermsRepository) {
		this.jpaTermsRepository = jpaTermsRepository;
	}

	@Override
	public AdminTermsDto save(AdminTermsDto adminTermsDto) {
		Terms terms = fromAdminTermDtoToTerms(adminTermsDto);
		return fromTermsToAdminTermDto(jpaTermsRepository.save(terms));
	}

	public AdminTermsDto fromTermsToAdminTermDto(Terms terms) {
		return new AdminTermsDto(
			terms.getId(),
			terms.getCode(),
			terms.getTitle(),
			terms.getType(),
			terms.getStatus(),
			terms.getRowVersion(),
			terms.getExposedFrom(),
			terms.getExposedTo(),
			terms.getDisplayOrder(),
			terms.getCreatedAt(),
			terms.getUpdatedAt(),
			null);
	}

	public Terms fromAdminTermDtoToTerms(AdminTermsDto adminTermsDto) {
		Terms terms = new TermsBuilder()
			.code(adminTermsDto.code())
			.title(adminTermsDto.title())
			.type(adminTermsDto.type())
			.status(adminTermsDto.status())
			.exposedFrom(adminTermsDto.exposedFrom())
			.exposedTo(adminTermsDto.exposedTo())
			.displayOrder(adminTermsDto.displayOrder())
			.build();

		List<Clause> clauses = adminTermsDto.clauses().stream()
			.map(dto -> new ClauseBuilder()
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

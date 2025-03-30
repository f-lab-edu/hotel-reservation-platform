package com.reservation.common.terms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.reservation.common.clause.domain.Clause;
import com.reservation.common.clause.domain.Clause.ClauseBuilder;
import com.reservation.common.terms.domain.Clauses;
import com.reservation.common.terms.domain.Terms;
import com.reservation.common.terms.domain.Terms.TermsBuilder;
import com.reservation.commonapi.terms.repository.AdminTermsRepository;
import com.reservation.commonapi.terms.repository.dto.AdminClauseDto;
import com.reservation.commonapi.terms.repository.dto.AdminTermsDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;

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

	@Override
	public boolean existsByCodeAndStatus(TermsCode code, TermsStatus status) {
		return this.jpaTermsRepository.existsByCodeAndStatus(code, status);
	}

	@Override
	public Optional<Integer> findMaxVersionByCode(TermsCode code) {
		return this.jpaTermsRepository.findMaxVersionByCode(code);
	}

	@Override
	public Optional<AdminTermsDto> findById(Long id) {
		return this.jpaTermsRepository.findById(id).map(this::fromTermsToAdminTermDto);
	}

	public AdminTermsDto fromTermsToAdminTermDto(Terms terms) {
		List<AdminClauseDto> adminClauses = terms.getClauses()
			.stream()
			.map(c -> new AdminClauseDto(c.getId(), c.getClauseOrder(), c.getTitle(), c.getContent()))
			.toList();

		return new AdminTermsDto(
			terms.getId(),
			terms.getCode(),
			terms.getTitle(),
			terms.getType(),
			terms.getStatus(),
			terms.getVersion(),
			terms.getExposedFrom(),
			terms.getExposedTo(),
			terms.getDisplayOrder(),
			terms.getCreatedAt(),
			terms.getUpdatedAt(),
			adminClauses);
	}

	public Terms fromAdminTermDtoToTerms(AdminTermsDto adminTermsDto) {
		Terms terms = new TermsBuilder()
			.code(adminTermsDto.code())
			.title(adminTermsDto.title())
			.type(adminTermsDto.type())
			.status(adminTermsDto.status())
			.version(adminTermsDto.version())
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

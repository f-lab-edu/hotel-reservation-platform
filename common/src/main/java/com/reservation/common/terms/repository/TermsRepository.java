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
import com.reservation.commonapi.terms.repository.dto.ClauseDto;
import com.reservation.commonapi.terms.repository.dto.TermsDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;

@Repository
public class TermsRepository implements AdminTermsRepository {

	private final JpaTermsRepository jpaTermsRepository;

	public TermsRepository(JpaTermsRepository jpaTermsRepository) {
		this.jpaTermsRepository = jpaTermsRepository;
	}

	@Override
	public TermsDto save(TermsDto termsDto) {
		Terms terms = fromAdminTermDtoToTerms(termsDto);
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
	public Optional<TermsDto> findById(Long id) {
		return this.jpaTermsRepository.findById(id).map(this::fromTermsToAdminTermDto);
	}

	@Override
	public Optional<TermsDto> findByCodeAndStatus(TermsCode code, TermsStatus termsStatus) {
		return this.jpaTermsRepository.findByCodeAndStatus(code, termsStatus).map(this::fromTermsToAdminTermDto);
	}

	public TermsDto fromTermsToAdminTermDto(Terms terms) {
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
			terms.getExposedFrom(),
			terms.getExposedTo(),
			terms.getDisplayOrder(),
			terms.getCreatedAt(),
			terms.getUpdatedAt(),
			adminClauses);
	}

	public Terms fromAdminTermDtoToTerms(TermsDto termsDto) {
		Terms terms = new TermsBuilder()
			.code(termsDto.code())
			.title(termsDto.title())
			.type(termsDto.type())
			.status(termsDto.status())
			.version(termsDto.version())
			.exposedFrom(termsDto.exposedFrom())
			.exposedTo(termsDto.exposedTo())
			.displayOrder(termsDto.displayOrder())
			.build();

		List<Clause> clauses = termsDto.clauses().stream()
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

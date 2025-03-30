package com.reservation.common.terms.repository;

import static com.reservation.common.support.sort.SortUtils.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.reservation.common.clause.domain.Clause;
import com.reservation.common.clause.domain.Clause.ClauseBuilder;
import com.reservation.common.terms.domain.Clauses;
import com.reservation.common.terms.domain.QTerms;
import com.reservation.common.terms.domain.Terms;
import com.reservation.common.terms.domain.Terms.TermsBuilder;
import com.reservation.commonapi.terms.query.condition.AdminTermsQueryCondition;
import com.reservation.commonapi.terms.repository.AdminTermsRepository;
import com.reservation.commonapi.terms.repository.dto.AdminTermsDto;
import com.reservation.commonapi.terms.repository.dto.QAdminTermsDto;
import com.reservation.commonmodel.terms.ClauseDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;

@Repository
public class TermsRepository implements AdminTermsRepository {

	private final JPAQueryFactory queryFactory;
	private final JpaTermsRepository jpaTermsRepository;

	public TermsRepository(JPAQueryFactory queryFactory, JpaTermsRepository jpaTermsRepository) {
		this.queryFactory = queryFactory;
		this.jpaTermsRepository = jpaTermsRepository;
	}

	@Override
	public TermsDto save(TermsDto adminTermsDto) {
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
	public Optional<TermsDto> findById(Long id) {
		return this.jpaTermsRepository.findById(id).map(this::fromTermsToAdminTermDto);
	}

	@Override
	public Page<AdminTermsDto> findTermsByCondition(AdminTermsQueryCondition condition) {
		QTerms terms = QTerms.terms;

		BooleanBuilder builder = new BooleanBuilder();

		// 약관 코드로 필터링
		if (condition.code() != null) {
			builder.and(terms.code.eq(condition.code()));
		}

		// 최신 버전만 필터링
		if (!condition.includeAllVersions()) {
			builder.and(terms.isLatest.isTrue());
		}

		Pageable pageable = condition.pageRequest();
		// 정렬 조건 생성
		List<OrderSpecifier<?>> orders = getOrderSpecifiers(condition.pageRequest().getSort(), Terms.class, "terms");

		// 데이터 조회
		List<AdminTermsDto> results = queryFactory
			.select(new QAdminTermsDto(
				terms.id,
				terms.code,
				terms.title,
				terms.type,
				terms.status,
				terms.version,
				terms.isLatest,
				terms.exposedFrom,
				terms.exposedTo,
				terms.displayOrder,
				terms.createdAt
			))
			.from(terms)
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(orders.toArray(new OrderSpecifier[0]))
			.fetch();

		// count 쿼리 (총 개수)
		Long total = queryFactory
			.select(terms.count())
			.from(terms)
			.where(builder)
			.fetchOne();

		// Page 객체로 감싸기
		return new PageImpl<>(results, pageable, total != null ? total : 0);
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
			terms.getIsLatest(),
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
			.isLatest(termsDto.isLatest())
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

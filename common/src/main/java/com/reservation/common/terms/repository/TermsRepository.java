package com.reservation.common.terms.repository;

import static com.reservation.common.support.sort.SortUtils.*;
import static com.reservation.common.terms.repository.mapper.TermsMapper.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.reservation.common.support.cursor.CursorUtils;
import com.reservation.common.terms.domain.QClause;
import com.reservation.common.terms.domain.QTerms;
import com.reservation.common.terms.domain.Terms;
import com.reservation.commonapi.admin.query.AdminTermsKeysetQueryCondition;
import com.reservation.commonapi.admin.query.AdminTermsQueryCondition;
import com.reservation.commonapi.admin.query.cursor.AdminTermsCursor;
import com.reservation.commonapi.admin.repository.AdminTermsRepository;
import com.reservation.commonapi.admin.repository.dto.AdminTermsDto;
import com.reservation.commonapi.admin.repository.dto.QAdminTermsDto;
import com.reservation.commonapi.customer.query.CustomerTermsQueryCondition;
import com.reservation.commonapi.customer.repository.CustomerTermsRepository;
import com.reservation.commonapi.customer.repository.dto.CustomerTermsDto;
import com.reservation.commonapi.customer.repository.dto.QCustomerTermsDto;
import com.reservation.commonmodel.keyset.KeysetPage;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TermsRepository implements AdminTermsRepository, CustomerTermsRepository {
	private final JPAQueryFactory queryFactory;
	private final JpaTermsRepository jpaTermsRepository;

	@Override
	public TermsDto save(TermsDto termsDto) {
		Terms terms = fromDtoToEntity(termsDto);
		return fromEntityToDto(jpaTermsRepository.save(terms), false);
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
		return this.jpaTermsRepository.findById(id).map((terms) -> fromEntityToDto(terms, true));
	}

	@Override
	public List<TermsDto> findRequiredTerms() {
		return this.jpaTermsRepository.findByTypeAndStatus(TermsType.REQUIRED, TermsStatus.ACTIVE)
			.stream()
			.map((terms) -> fromEntityToDto(terms, false))
			.toList();
	}

	@Override
	public List<TermsDto> findByStatusAndIsLatest(TermsStatus status, Boolean isLatest) {
		return this.jpaTermsRepository.findByStatusAndIsLatest(status, isLatest)
			.stream()
			.map((terms) -> fromEntityToDto(terms, false))
			.toList();
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
				terms.exposedToOrNull,
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

	@Override
	public KeysetPage<AdminTermsDto, AdminTermsCursor> findTermsByKeysetCondition(
		AdminTermsKeysetQueryCondition condition) {
		QTerms terms = QTerms.terms;
		// 커서 조건을 위한 빌더
		BooleanBuilder cursorPredicate = CursorUtils.getCursorPredicate(condition.cursors(), QTerms.class, "terms");

		// 커서 외 조건을 위한 빌더
		BooleanBuilder builder = new BooleanBuilder();
		// 약관 코드로 필터링
		if (condition.code() != null) {
			builder.and(terms.code.eq(condition.code()));
		}

		// 최신 버전만 필터링
		if (!condition.includeAllVersions()) {
			builder.and(terms.isLatest.isTrue());
		}

		builder.and(cursorPredicate);
		List<OrderSpecifier> orderSpecifiers = CursorUtils.getOrderSpecifiers(condition.cursors(), QTerms.class,
			"terms");

		int size = condition.size();
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
				terms.exposedToOrNull,
				terms.displayOrder,
				terms.createdAt
			))
			.from(terms)
			.where(builder)
			.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
			.limit(size + 1)
			.fetch();

		boolean hasNext = results.size() > size;
		AdminTermsDto lastRow = hasNext
			? results.removeLast()
			: null;

		List<AdminTermsCursor> nextCursors = lastRow != null ? condition.cursors().stream()
			.map(cursor -> new AdminTermsCursor(cursor.cursorField(), cursor.direction(),
				cursor.cursorField().resolveNextCursorValue(lastRow))).toList() : null;

		return new KeysetPage<>(
			results,
			hasNext,
			hasNext ? nextCursors : null
		);
	}

	@Override
	public Optional<TermsDto> findWithClausesById(Long id) {
		QTerms terms = QTerms.terms;
		QClause clause = QClause.clause;

		Terms result = queryFactory
			.selectFrom(terms)
			.leftJoin(terms.clauseList, clause).fetchJoin() // fetch join으로 N+1 방지
			.where(terms.id.eq(id))
			.fetchOne();
		Optional<Terms> optionalResult = Optional.ofNullable(result);
		return optionalResult.map((terms1) -> fromEntityToDto(terms1, true));
	}

	@Override
	public Page<CustomerTermsDto> findTermsByCondition(CustomerTermsQueryCondition condition) {
		QTerms terms = QTerms.terms;
		QClause clause = QClause.clause;

		// 커서 외 조건을 위한 빌더
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(terms.isLatest.isTrue());
		builder.and(terms.status.eq(TermsStatus.ACTIVE));

		Pageable pageable = condition.pageRequest();
		// 정렬 조건 생성
		List<OrderSpecifier<?>> orders = getOrderSpecifiers(condition.pageRequest().getSort(), Terms.class, "terms");

		// 데이터 조회
		List<CustomerTermsDto> results = queryFactory
			.select(new QCustomerTermsDto(
				terms.id,
				terms.code,
				terms.title,
				terms.type,
				terms.status,
				terms.version,
				terms.exposedFrom,
				terms.displayOrder
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
}

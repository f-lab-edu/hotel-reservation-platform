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
import com.reservation.common.clause.domain.QClause;
import com.reservation.common.terms.domain.QTerms;
import com.reservation.common.terms.domain.Terms;
import com.reservation.common.terms.repository.cursorutil.AdminTermsCursorUtils;
import com.reservation.common.terms.repository.mapper.TermsDtoMapper;
import com.reservation.commonapi.admin.query.AdminTermsKeysetQueryCondition;
import com.reservation.commonapi.admin.query.AdminTermsQueryCondition;
import com.reservation.commonapi.admin.query.sort.AdminTermsSortCursor;
import com.reservation.commonapi.admin.repository.AdminTermsRepository;
import com.reservation.commonapi.admin.repository.dto.AdminTermsDto;
import com.reservation.commonapi.admin.repository.dto.QAdminTermsDto;
import com.reservation.commonmodel.keyset.KeysetPage;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;

@Repository
public class TermsRepository implements AdminTermsRepository {

	private final JPAQueryFactory queryFactory;
	private final JpaTermsRepository jpaTermsRepository;
	private final QTerms terms = QTerms.terms;
	private final QClause clause = QClause.clause;

	public TermsRepository(JPAQueryFactory queryFactory, JpaTermsRepository jpaTermsRepository) {
		this.queryFactory = queryFactory;
		this.jpaTermsRepository = jpaTermsRepository;
	}

	@Override
	public TermsDto save(TermsDto termsDto) {
		Terms terms = TermsDtoMapper.ToTerms(termsDto);
		return TermsDtoMapper.fromTerms(jpaTermsRepository.save(terms));
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
		return this.jpaTermsRepository.findById(id).map(TermsDtoMapper::fromTerms);
	}

	@Override
	public Page<AdminTermsDto> findTermsByCondition(AdminTermsQueryCondition condition) {
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
	public KeysetPage<AdminTermsDto, AdminTermsSortCursor> findTermsByKeysetCondition(
		AdminTermsKeysetQueryCondition condition) {
		// 커서 조건을 위한 빌더
		BooleanBuilder cursorPredicate = AdminTermsCursorUtils.getCursorPredicate(condition);

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

		List<OrderSpecifier<?>> orderSpecifiers = AdminTermsCursorUtils.getOrderSpecifiers(condition);

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
			? results.remove(results.size() - 1)
			: null;

		List<AdminTermsSortCursor> nextCursors = AdminTermsCursorUtils.getNextCursors(condition, lastRow);

		return new KeysetPage<>(
			results,
			hasNext,
			hasNext ? nextCursors : null
		);
	}

	@Override
	public Optional<TermsDto> findWithClausesById(Long id) {
		Terms result = queryFactory
			.selectFrom(terms)
			.leftJoin(terms.clauseList, clause).fetchJoin() // fetch join으로 N+1 방지
			.where(terms.id.eq(id))
			.fetchOne();
		Optional<Terms> optionalResult = Optional.ofNullable(result);
		return optionalResult.map(TermsDtoMapper::fromTerms);
	}
}

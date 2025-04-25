package com.reservation.admin.terms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.reservation.admin.terms.controller.request.TermsCursor;
import com.reservation.admin.terms.service.dto.QSearchTermsResult;
import com.reservation.admin.terms.service.dto.SearchTermsResult;
import com.reservation.domain.terms.QClause;
import com.reservation.domain.terms.QTerms;
import com.reservation.domain.terms.Terms;
import com.reservation.domain.terms.enums.TermsCode;
import com.reservation.querysupport.cursor.CursorUtils;
import com.reservation.querysupport.page.KeysetPage;
import com.reservation.querysupport.sort.SortUtils;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TermsQueryRepository {
	private final JPAQueryFactory queryFactory;

	public Optional<Terms> findWithClausesById(Long id) {
		QTerms terms = QTerms.terms;
		QClause clause = QClause.clause;

		Terms result = queryFactory
			.selectFrom(terms)
			.leftJoin(terms.clauses, clause).fetchJoin() // fetch join -> N+1 방지
			.where(terms.id.eq(id))
			.fetchOne();

		return Optional.ofNullable(result);
	}

	public Page<SearchTermsResult> searchTermsFormatPage
		(TermsCode searchCodeOrNull, boolean isLatest, PageRequest pageRequest) {

		QTerms terms = QTerms.terms;
		BooleanBuilder builder = new BooleanBuilder();

		// 약관 코드로 필터링
		if (searchCodeOrNull != null) {
			builder.and(terms.code.eq(searchCodeOrNull));
		}
		// Default 모든 버전 조회 -> isLatestOrNull == true 최신 버전만 필터링
		if (isLatest) {
			builder.and(terms.isLatest.isTrue());
		}

		// 정렬 조건 생성
		List<OrderSpecifier<?>> orders = SortUtils.getOrderSpecifiers(pageRequest.getSort(), Terms.class, "terms");

		// 데이터 조회
		List<SearchTermsResult> searchTermResults = queryFactory
			.select(new QSearchTermsResult(
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
			.offset(pageRequest.getOffset())
			.limit(pageRequest.getPageSize())
			.orderBy(orders.toArray(new OrderSpecifier[0]))
			.fetch();

		// count 쿼리 (총 개수)
		Long total = queryFactory
			.select(terms.count())
			.from(terms)
			.where(builder)
			.fetchOne();

		// Page 객체로 감싸기
		return new PageImpl<>(searchTermResults, pageRequest, total != null ? total : 0);
	}

	public KeysetPage<SearchTermsResult, TermsCursor> findTermsByKeysetCondition
		(TermsCode searchCodeOrNull, boolean isLatest, int size, List<TermsCursor> cursors) {
		QTerms terms = QTerms.terms;
		// 커서 조건을 위한 빌더
		BooleanBuilder cursorPredicate = CursorUtils.getCursorPredicate(cursors, QTerms.class, "terms");

		// 커서 외 조건을 위한 빌더
		BooleanBuilder builder = new BooleanBuilder();

		// 약관 코드로 필터링
		if (searchCodeOrNull != null) {
			builder.and(terms.code.eq(searchCodeOrNull));
		}
		// Default 모든 버전 조회 -> isLatestOrNull == true 최신 버전만 필터링
		if (isLatest) {
			builder.and(terms.isLatest.isTrue());
		}

		builder.and(cursorPredicate);
		List<OrderSpecifier> orderSpecifiers = CursorUtils.getOrderSpecifiers(cursors, QTerms.class, "terms");

		// 데이터 조회
		List<SearchTermsResult> searchTermResults = queryFactory
			.select(new QSearchTermsResult(
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

		boolean hasNext = searchTermResults.size() > size;
		SearchTermsResult lastRow = hasNext
			? searchTermResults.removeLast()
			: null;

		List<TermsCursor> nextCursors = lastRow != null ? cursors.stream()
			.map(cursor -> new TermsCursor(cursor.cursorField(), cursor.direction(),
				cursor.cursorField().resolveNextCursorValue(lastRow))).toList() : null;

		return new KeysetPage<>(
			searchTermResults,
			hasNext,
			hasNext ? nextCursors : null
		);
	}
}

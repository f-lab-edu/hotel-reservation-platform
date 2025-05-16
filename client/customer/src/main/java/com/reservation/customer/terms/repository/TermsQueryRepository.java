package com.reservation.customer.terms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.reservation.customer.terms.repository.dto.QSearchTerms;
import com.reservation.customer.terms.repository.dto.SearchTerms;
import com.reservation.domain.terms.QClause;
import com.reservation.domain.terms.QTerms;
import com.reservation.domain.terms.Terms;
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

	public Page<SearchTerms> findTermsByCondition(PageRequest pageRequest) {
		QTerms terms = QTerms.terms;
		BooleanBuilder builder = new BooleanBuilder();

		// 정렬 조건 생성
		List<OrderSpecifier<?>> orders = SortUtils.getOrderSpecifiers(pageRequest.getSort(), Terms.class, "terms");

		// 데이터 조회
		List<SearchTerms> results = queryFactory
			.select(new QSearchTerms(
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
		return new PageImpl<>(results, pageRequest, total != null ? total : 0);
	}
}

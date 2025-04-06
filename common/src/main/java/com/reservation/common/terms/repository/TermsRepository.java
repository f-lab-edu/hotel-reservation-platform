package com.reservation.common.terms.repository;

import static com.reservation.common.support.sort.SortUtils.*;
import static org.springframework.data.domain.Sort.Direction.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.reservation.common.clause.domain.Clause;
import com.reservation.common.clause.domain.Clause.ClauseBuilder;
import com.reservation.common.terms.domain.Clauses;
import com.reservation.common.terms.domain.QTerms;
import com.reservation.common.terms.domain.Terms;
import com.reservation.common.terms.domain.Terms.TermsBuilder;
import com.reservation.commonapi.admin.query.AdminTermsKeysetQueryCondition;
import com.reservation.commonapi.admin.query.AdminTermsQueryCondition;
import com.reservation.commonapi.admin.query.sort.AdminTermsSortCursor;
import com.reservation.commonapi.admin.query.sort.AdminTermsSortField;
import com.reservation.commonapi.admin.repository.AdminTermsRepository;
import com.reservation.commonapi.admin.repository.dto.AdminTermsDto;
import com.reservation.commonapi.admin.repository.dto.QAdminTermsDto;
import com.reservation.commonmodel.keyset.KeysetPage;
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

	private ComparableExpressionBase<?> getFieldExpression(QTerms terms, AdminTermsSortCursor sort) {
		return switch (sort.field()) {
			case CREATED_AT -> terms.createdAt;
			case EXPOSED_FROM -> terms.exposedFrom;
			case EXPOSED_TO -> terms.exposedToOrNull;
			case TITLE -> terms.title;
			case ID -> terms.id;
		};
	}

	private Comparable parseCursor(AdminTermsSortCursor sort) {
		return sort.field().parseCursor(sort.cursor());
	}

	private String getCursorValueFrom(AdminTermsDto row, AdminTermsSortCursor sort) {
		// 정렬 필드 기준으로 커서 값을 추출한다
		return switch (sort.field()) {
			case CREATED_AT -> row.getCreatedAt().toString();
			case EXPOSED_FROM -> row.getExposedFrom().toString();
			case EXPOSED_TO -> row.getExposedToOrNull() != null
				? row.getExposedToOrNull().toString()
				: "";
			case TITLE -> row.getTitle();
			case ID -> String.valueOf(row.getId());
		};
	}

	@Override
	public KeysetPage<AdminTermsDto, AdminTermsSortCursor> findTermsByKeysetCondition(
		AdminTermsKeysetQueryCondition condition) {

		QTerms terms = QTerms.terms;

		BooleanBuilder cursorPredicate = new BooleanBuilder(); // 커서 조건을 위한 빌더

		List<AdminTermsSortCursor> sortCursors =
			condition.sortCursors() == null ?
				List.of(new AdminTermsSortCursor(AdminTermsSortField.CREATED_AT, DESC, null),
					new AdminTermsSortCursor(AdminTermsSortField.ID, DESC, null)) :
				condition.sortCursors();

		for (int i = 0; i < sortCursors.size(); i++) {
			BooleanBuilder andBuilder = new BooleanBuilder();
			for (int j = 0; j < i; j++) {
				// 이전 커서 조건 eq
				AdminTermsSortCursor beforeSortCursor = sortCursors.get(j);
				if (beforeSortCursor.cursor() == null) {
					// 커서가 null인 경우는 커서 조건을 추가하지 않음
					continue;
				}
				Comparable beforeCursor = parseCursor(beforeSortCursor);
				ComparableExpression beforeField = (ComparableExpression)((Path)getFieldExpression(terms,
					beforeSortCursor));
				andBuilder.and(beforeField.eq(beforeCursor));
			}

			// 현재 커서 조건 범위 설정

			AdminTermsSortCursor currentSortCursor = sortCursors.get(i);
			if (currentSortCursor.cursor() == null) {
				// 커서가 null인 경우는 커서 조건을 추가하지 않음
				continue;
			}
			Comparable currentCursor = parseCursor(currentSortCursor);
			ComparableExpressionBase currentField = getFieldExpression(terms, currentSortCursor);
			if (currentField instanceof DateTimePath<?>) {
				DateTimePath<LocalDateTime> dateTimePath = ((DateTimePath<LocalDateTime>)currentField);
				LocalDateTime dateTime = (LocalDateTime)currentCursor;
				if (currentSortCursor.direction() == DESC) {
					andBuilder.and(dateTimePath.loe(dateTime));
				} else {
					andBuilder.and(dateTimePath.goe(dateTime));
				}
			} else if (currentField instanceof NumberPath<?>) {
				NumberPath<Long> numberPath = ((NumberPath<Long>)currentField);
				Long id = (Long)currentCursor;
				if (currentSortCursor.direction() == DESC) {
					andBuilder.and(numberPath.loe(id));
				} else {
					andBuilder.and(numberPath.goe(id));
				}
			}

			// 커서 조건을 추가
			cursorPredicate.or(andBuilder);
		}

		BooleanBuilder builder = new BooleanBuilder();
		// 약관 코드로 필터링
		if (condition.code() != null) {
			builder.and(terms.code.eq(condition.code()));
		}

		// 최신 버전만 필터링
		if (!condition.includeAllVersions()) {
			builder.and(terms.isLatest.isTrue());
		}

		List<OrderSpecifier<?>> orderSpecifiers = sortCursors.stream()
			.map(sort -> {
				ComparableExpressionBase<?> expr = getFieldExpression(terms, sort);
				return sort.direction().isAscending()
					? expr.asc()
					: expr.desc();
			})
			.toList();
		builder.and(cursorPredicate);

		int size = condition.size() == null ? 2 : condition.size();
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
			.offset(0) // 커서 기반 페이징은 offset을 0으로 설정
			.limit(size + 1) // 다음 커서를 위해 +1
			.fetch();

		boolean hasNext = results.size() > size;
		AdminTermsDto lastRow = hasNext
			? results.remove(results.size() - 1)
			: null;

		List<AdminTermsSortCursor> nextCursors = lastRow != null ? sortCursors.stream()
			.map(sortCursor ->
				new AdminTermsSortCursor(sortCursor.field(), sortCursor.direction(),
					getCursorValueFrom(lastRow, sortCursor)))
			.toList() : null;

		return new KeysetPage<>(
			results,
			hasNext,
			hasNext ? nextCursors : null
		);
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
			terms.getExposedToOrNull(),
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
			.exposedToOrNull(termsDto.exposedToOrNull())
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

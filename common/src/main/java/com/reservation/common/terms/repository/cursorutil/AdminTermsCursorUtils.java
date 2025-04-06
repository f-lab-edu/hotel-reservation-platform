package com.reservation.common.terms.repository.cursorutil;

import static org.springframework.data.domain.Sort.Direction.*;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.reservation.common.terms.domain.QTerms;
import com.reservation.commonapi.admin.query.AdminTermsKeysetQueryCondition;
import com.reservation.commonapi.admin.query.sort.AdminTermsSortCursor;
import com.reservation.commonapi.admin.repository.dto.AdminTermsDto;

public class AdminTermsCursorUtils {
	private static final QTerms terms = QTerms.terms;

	public static BooleanBuilder getCursorPredicate(AdminTermsKeysetQueryCondition condition) {
		BooleanBuilder cursorPredicate = new BooleanBuilder();
		List<AdminTermsSortCursor> sortCursors = condition.sortCursors();

		for (int i = 0; i < sortCursors.size(); i++) {
			BooleanBuilder andBuilder = new BooleanBuilder();
			for (int j = 0; j < i; j++) {
				// 이전 커서 조건 eq
				AdminTermsSortCursor beforeSortCursor = sortCursors.get(j);
				addBeforeCursorExpression(beforeSortCursor, andBuilder);
			}

			// 현재 커서 조건 범위 설정
			AdminTermsSortCursor currentSortCursor = sortCursors.get(i);
			addCurrentCursorExpression(currentSortCursor, andBuilder);

			// 커서 조건을 추가
			cursorPredicate.or(andBuilder);
		}
		return cursorPredicate;
	}

	public static List<OrderSpecifier<?>> getOrderSpecifiers(AdminTermsKeysetQueryCondition condition) {
		return condition.sortCursors().stream()
			.map(sortCursor -> {
				ComparableExpressionBase<?> expr = getFieldExpression(sortCursor);
				return sortCursor.direction().isAscending()
					? expr.asc()
					: expr.desc();
			})
			.toList();
	}

	public static List<AdminTermsSortCursor> getNextCursors(AdminTermsKeysetQueryCondition condition,
		AdminTermsDto lastRow) {
		return lastRow != null ? condition.sortCursors().stream()
			.map(sortCursor ->
				new AdminTermsSortCursor(sortCursor.field(), sortCursor.direction(),
					getCursorValueFrom(lastRow, sortCursor)))
			.toList() : null;
	}

	private static ComparableExpressionBase<?> getFieldExpression(AdminTermsSortCursor sortCursor) {
		return switch (sortCursor.field()) {
			case CREATED_AT -> terms.createdAt;
			case EXPOSED_FROM -> terms.exposedFrom;
			case EXPOSED_TO -> terms.exposedToOrNull;
			case TITLE -> terms.title;
			case ID -> terms.id;
		};
	}

	private static void addBeforeCursorExpression(AdminTermsSortCursor beforeSortCursor, BooleanBuilder andBuilder) {
		if (beforeSortCursor.cursor() == null) {
			// 커서 값이 null인 경우는 커서 조건을 추가하지 않음
			return;
		}
		Comparable beforeCursor = parseCursor(beforeSortCursor);
		ComparableExpressionBase beforeField = getFieldExpression(beforeSortCursor);
		andBuilder.and(beforeField.eq(beforeCursor));
	}

	private static void addCurrentCursorExpression(AdminTermsSortCursor currentSortCursor, BooleanBuilder andBuilder) {
		// 커서 값이 null -> 커서 조건을 추가하지 않음
		if (currentSortCursor.cursor() == null) {
			return;
		}

		Comparable currentCursor = parseCursor(currentSortCursor);
		ComparableExpressionBase currentField = getFieldExpression(currentSortCursor);

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
	}

	private static Comparable parseCursor(AdminTermsSortCursor sortCursor) {
		return sortCursor.field().parseCursor(sortCursor.cursor());
	}

	private static String getCursorValueFrom(AdminTermsDto row, AdminTermsSortCursor sort) {
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
}

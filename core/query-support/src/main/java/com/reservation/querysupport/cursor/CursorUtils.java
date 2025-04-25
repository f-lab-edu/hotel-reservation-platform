package com.reservation.querysupport.cursor;

import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;

public class CursorUtils {
	public static <T> BooleanBuilder getCursorPredicate(List<? extends Cursor> cursors, Class<T> domainClass,
		String alias) {
		BooleanBuilder cursorPredicate = new BooleanBuilder();
		PathBuilder<T> pathBuilder = new PathBuilder<>(domainClass, alias);

		int cursorCount = cursors.size();
		for (int i = 0; i < cursorCount; i++) {
			Cursor currentCursor = cursors.get(i);
			if (currentCursor.value() == null) {
				continue;
			}
			BooleanBuilder addCursorPredicate = new BooleanBuilder();

			for (int j = 0; j < i; j++) {
				// 이전 커서 조건 eq 반복
				Cursor beforeCursor = cursors.get(j);
				if (beforeCursor.value() == null) {
					continue;
				}
				CursorPathType pathType = beforeCursor.cursorField().pathType();
				BooleanExpression beforeCursorExpression = pathType.getBeforeCursorExpression(pathBuilder,
					beforeCursor);

				addCursorPredicate.and(beforeCursorExpression);
			}

			// 현재 커서 조건 설정
			CursorPathType pathType = currentCursor.cursorField().pathType();
			BooleanExpression currentCursorExpression = pathType.getCurrentCursorExpression(pathBuilder, currentCursor);

			addCursorPredicate.and(currentCursorExpression);

			// 커서 조건 하나 세팅
			cursorPredicate.or(addCursorPredicate);
		}

		return cursorPredicate;
	}

	public static <T> List<OrderSpecifier> getOrderSpecifiers(List<? extends Cursor> cursors,
		Class<T> domainClass,
		String alias) {
		PathBuilder<T> pathBuilder = new PathBuilder<>(domainClass, alias);
		return cursors.stream()
			.map(cursor -> cursor.cursorField().pathType().getOrderSpecifier(pathBuilder, cursor))
			.toList();
	}
}

package com.reservation.querysupport.cursor;

import org.springframework.data.domain.Sort.Direction;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.reservation.support.exception.ErrorCode;

public enum CursorPathType {
	BOOLEAN,
	DATE_TIME,
	STRING,
	NUMBER,
	COMPARABLE,
	ENUM,
	DATE;

	public BooleanExpression getBeforeCursorExpression(PathBuilder pathBuilder, Cursor cursor) {
		CursorField cursorField = cursor.cursorField();
		String propertyName = cursorField.fieldName();
		Class<? extends Comparable> propertyClass = cursorField.fieldType();
		Comparable propertyValue = cursorField.parseCursor(cursor.value());

		switch (this) {
			case DATE_TIME -> {
				return pathBuilder.getDateTime(propertyName, propertyClass).eq(propertyValue);
			}
			case STRING -> {
				return pathBuilder.getString(propertyName).eq((Expression<String>)propertyValue);
			}
			case NUMBER -> {
				return pathBuilder.getNumber(propertyName, propertyClass).eq(propertyValue);
			}
			case COMPARABLE -> {
				return pathBuilder.getComparable(propertyName, propertyClass).eq(propertyValue);
			}
			case ENUM -> {
				return pathBuilder.getEnum(propertyName, propertyClass).loe(propertyValue);
			}
			case DATE -> {
				return pathBuilder.getDate(propertyName, propertyClass).loe(propertyValue);
			}
			case BOOLEAN -> {
				return pathBuilder.getBoolean(propertyName).loe((Expression<Boolean>)propertyValue);
			}
			default -> throw ErrorCode.INTERNAL_SERVER_ERROR.exception("커서 타입이 잘못되었습니다.");
		}
	}

	public BooleanExpression getCurrentCursorExpression(PathBuilder pathBuilder, Cursor cursor) {
		CursorField cursorField = cursor.cursorField();
		String propertyName = cursorField.fieldName();
		Class<? extends Comparable> propertyClass = cursorField.fieldType();
		Comparable propertyValue = cursorField.parseCursor(cursor.value());
		Direction direction = cursor.direction();

		switch (this) {
			case DATE_TIME -> {
				return direction.isAscending() ?
					pathBuilder.getDateTime(propertyName, propertyClass).goe(propertyValue) :
					pathBuilder.getDateTime(propertyName, propertyClass).loe(propertyValue);
			}
			case STRING -> {
				return direction.isAscending() ?
					pathBuilder.getString(propertyName).goe((Expression<String>)propertyValue) :
					pathBuilder.getString(propertyName).loe((Expression<String>)propertyValue);
			}
			case NUMBER -> {
				return direction.isAscending() ?
					pathBuilder.getNumber(propertyName, propertyClass).goe((Number)propertyValue) :
					pathBuilder.getNumber(propertyName, propertyClass).loe((Number)propertyValue);
			}
			case COMPARABLE -> {
				return direction.isAscending() ?
					pathBuilder.getComparable(propertyName, propertyClass).goe(propertyValue) :
					pathBuilder.getComparable(propertyName, propertyClass).loe(propertyValue);
			}
			case ENUM -> {
				return direction.isAscending() ?
					pathBuilder.getEnum(propertyName, propertyClass).goe(propertyValue) :
					pathBuilder.getEnum(propertyName, propertyClass).loe(propertyValue);
			}
			case DATE -> {
				return direction.isAscending() ?
					pathBuilder.getDate(propertyName, propertyClass).goe(propertyValue) :
					pathBuilder.getDate(propertyName, propertyClass).loe(propertyValue);
			}
			case BOOLEAN -> {
				return direction.isAscending() ?
					pathBuilder.getBoolean(propertyName).goe((Expression<Boolean>)propertyValue) :
					pathBuilder.getBoolean(propertyName).loe((Expression<Boolean>)propertyValue);
			}
			default -> throw ErrorCode.INTERNAL_SERVER_ERROR.exception("커서 타입이 잘못되었습니다.");
		}
	}

	public OrderSpecifier getOrderSpecifier(PathBuilder pathBuilder, Cursor cursor) {
		CursorField cursorField = cursor.cursorField();
		String propertyName = cursorField.fieldName();
		Class<? extends Comparable> propertyClass = cursorField.fieldType();
		Direction direction = cursor.direction();

		switch (this) {
			case DATE_TIME -> {
				return direction.isAscending() ?
					pathBuilder.getDateTime(propertyName, propertyClass).asc() :
					pathBuilder.getDateTime(propertyName, propertyClass).desc();
			}
			case STRING -> {
				return direction.isAscending() ?
					pathBuilder.getString(propertyName).asc() :
					pathBuilder.getString(propertyName).desc();
			}
			case NUMBER -> {
				return direction.isAscending() ?
					pathBuilder.getNumber(propertyName, propertyClass).asc() :
					pathBuilder.getNumber(propertyName, propertyClass).desc();
			}
			case COMPARABLE -> {
				return direction.isAscending() ?
					pathBuilder.getComparable(propertyName, propertyClass).asc() :
					pathBuilder.getComparable(propertyName, propertyClass).desc();
			}
			case ENUM -> {
				return direction.isAscending() ?
					pathBuilder.getEnum(propertyName, propertyClass).asc() :
					pathBuilder.getEnum(propertyName, propertyClass).desc();
			}
			case DATE -> {
				return direction.isAscending() ?
					pathBuilder.getDate(propertyName, propertyClass).asc() :
					pathBuilder.getDate(propertyName, propertyClass).desc();
			}
			case BOOLEAN -> {
				return direction.isAscending() ?
					pathBuilder.getBoolean(propertyName).asc() :
					pathBuilder.getBoolean(propertyName).desc();
			}
			default -> throw ErrorCode.INTERNAL_SERVER_ERROR.exception("커서 타입이 잘못되었습니다.");
		}
	}
}


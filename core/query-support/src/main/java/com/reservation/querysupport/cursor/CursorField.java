package com.reservation.querysupport.cursor;

public interface CursorField<T> {
	String fieldName();

	Class<? extends Comparable> fieldType();

	CursorPathType pathType();

	Comparable parseCursor(String stringValue);

	String resolveNextCursorValue(T lastValue);
}

package com.reservation.commonmodel.cursor;

public interface CursorField {
	String fieldName();

	Class<? extends Comparable> fieldType();

	CursorPathType pathType();

	Comparable parseCursor(String stringValue);
}

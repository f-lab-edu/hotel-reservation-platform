package com.reservation.admin.terms.controller.request;

import org.springframework.data.domain.Sort;

import com.reservation.querysupport.cursor.Cursor;

public record TermsCursor(
	TermsCursorField cursorField,
	Sort.Direction direction,
	String value
) implements Cursor {
}

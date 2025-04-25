package com.reservation.querysupport.cursor;

import org.springframework.data.domain.Sort;

public interface Cursor {
	CursorField cursorField();

	String value();

	Sort.Direction direction();
}

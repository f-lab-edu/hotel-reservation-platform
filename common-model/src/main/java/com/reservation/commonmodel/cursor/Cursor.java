package com.reservation.commonmodel.cursor;

import org.springframework.data.domain.Sort.Direction;

public interface Cursor {
	CursorField cursorField();

	String value();

	Direction direction();
}

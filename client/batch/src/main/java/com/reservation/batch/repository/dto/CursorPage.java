package com.reservation.batch.repository.dto;

import java.util.List;

public record CursorPage<CONTENT, CURSOR>(
	List<CONTENT> content,
	CURSOR nextCursor
) {
	public boolean hasNext() {
		return nextCursor != null;
	}
}

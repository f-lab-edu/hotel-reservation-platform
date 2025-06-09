package com.reservation.batch.repository.dto;

public record Cursor<CONTENT, CURSOR>(
	CONTENT content,
	CURSOR nextCursor
) {
	public boolean hasNext() {
		return nextCursor != null;
	}
}

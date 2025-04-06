package com.reservation.commonapi.admin.query.sort;

import java.time.LocalDateTime;

import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.sort.SortField;

import lombok.Getter;

@Getter
public enum AdminTermsSortField implements SortField {
	EXPOSED_FROM("exposedFrom", LocalDateTime.class), // 노출 시작일
	EXPOSED_TO("exposedToOrNull", LocalDateTime.class), // 노출 종료일
	CREATED_AT("createdAt", LocalDateTime.class), // 생성일
	TITLE("title", String.class), // 제목
	ID("id", Long.class); // ID

	private final String fieldName;
	private final Class<?> cursorType;

	AdminTermsSortField(String fieldName, Class<?> cursorType) {
		this.fieldName = fieldName;
		this.cursorType = cursorType;
	}

	public Comparable parseCursor(String rawCursor) {
		if (rawCursor == null) {
			return null;
		}
		if (this.cursorType == String.class) {
			return rawCursor;
		}
		if (this.cursorType == LocalDateTime.class) {
			return LocalDateTime.parse(rawCursor);
		}
		if (this.cursorType == Long.class) {
			return Long.parseLong(rawCursor);
		}
		throw ErrorCode.BAD_REQUEST.exception("지원하지 않는 커서 타입입니다.");
	}
}

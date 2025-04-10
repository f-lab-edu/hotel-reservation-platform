package com.reservation.commonapi.admin.query.cursor;

import java.time.LocalDateTime;

import com.reservation.commonapi.admin.repository.dto.AdminTermsDto;
import com.reservation.commonmodel.cursor.CursorField;
import com.reservation.commonmodel.cursor.CursorPathType;
import com.reservation.commonmodel.exception.ErrorCode;

public enum AdminTermsCursorField implements CursorField {
	EXPOSED_FROM("exposedFrom", LocalDateTime.class, CursorPathType.DATE_TIME), // 노출 시작일
	EXPOSED_TO("exposedToOrNull", LocalDateTime.class, CursorPathType.DATE_TIME), // 노출 종료일
	CREATED_AT("createdAt", LocalDateTime.class, CursorPathType.DATE_TIME), // 생성일
	TITLE("title", String.class, CursorPathType.STRING), // 제목
	ID("id", Long.class, CursorPathType.NUMBER); // ID;

	private final String fieldName;
	private final Class<? extends Comparable> fieldType;
	private final CursorPathType cursorPathType;

	AdminTermsCursorField(String fieldName, Class<? extends Comparable> fieldType, CursorPathType cursorPathType) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.cursorPathType = cursorPathType;
	}

	@Override
	public String fieldName() {
		return fieldName;
	}

	@Override
	public Class<? extends Comparable> fieldType() {
		return fieldType;
	}

	@Override
	public CursorPathType pathType() {
		return this.cursorPathType;
	}

	public Comparable parseCursor(String rawCursor) {
		if (rawCursor == null) {
			return null;
		}
		if (this.fieldType == String.class) {
			return rawCursor;
		}
		if (this.fieldType == LocalDateTime.class) {
			return LocalDateTime.parse(rawCursor);
		}
		if (this.fieldType == Long.class) {
			return Long.parseLong(rawCursor);
		}
		throw ErrorCode.CONFLICT.exception("지원하지 않는 커서 타입입니다.");
	}

	public String resolveNextCursorValue(AdminTermsDto lastRow) {
		switch (this) {
			case CREATED_AT, EXPOSED_FROM, EXPOSED_TO -> {
				return lastRow.getExposedFrom().toString();
			}
			case TITLE -> {
				return lastRow.getTitle();
			}
			case ID -> {
				return String.valueOf(lastRow.getId());
			}
		}
		throw ErrorCode.CONFLICT.exception("지원하지 않는 커서 타입입니다.");
	}
}

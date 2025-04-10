package com.reservation.commonapi.admin.query;

import static org.springframework.data.domain.Sort.Direction.*;

import java.util.List;

import com.reservation.commonapi.admin.query.cursor.AdminTermsCursor;
import com.reservation.commonapi.admin.query.cursor.AdminTermsCursorField;
import com.reservation.commonmodel.terms.TermsCode;

public record AdminTermsKeysetQueryCondition(
	TermsCode code,
	boolean includeAllVersions,
	Integer size,
	List<AdminTermsCursor> cursors
) {
	public static final int DEFAULT_PAGE_SIZE = 2;

	public Integer size() {
		return size == null ? DEFAULT_PAGE_SIZE : size;
	}

	public List<AdminTermsCursor> cursors() {
		// 보조 정렬 조건 추가
		List<AdminTermsCursor> cursors = this.cursors != null ? this.cursors : List.of();
		List<AdminTermsCursorField> cursorFields = cursors.stream().map(AdminTermsCursor::cursorField).toList();

		if (!cursorFields.contains(AdminTermsCursorField.ID)) {
			cursors.add(new AdminTermsCursor(AdminTermsCursorField.ID, DESC, "1"));

		}
		return cursors;
	}
}

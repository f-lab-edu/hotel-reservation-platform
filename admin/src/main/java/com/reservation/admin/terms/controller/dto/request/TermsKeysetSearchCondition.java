package com.reservation.admin.terms.controller.dto.request;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;

import com.reservation.commonapi.admin.query.cursor.AdminTermsCursor;
import com.reservation.commonapi.admin.query.cursor.AdminTermsCursorField;
import com.reservation.commonmodel.terms.TermsCode;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TermsKeysetSearchCondition(
	@Nullable
	TermsCode code,

	@Nullable
	Boolean includeAllVersions,

	@Min(10) @Max(100)
	@Nullable
	Integer size,

	@Nullable
	List<AdminTermsCursor> cursors
) {
	public int defaultSize() {
		return 10;
	}

	public List<AdminTermsCursor> cursors() {
		List<AdminTermsCursor> cursors =
			this.cursors == null || this.cursors.isEmpty() ? new ArrayList<>() : this.cursors;
		List<AdminTermsCursorField> fields = cursors.stream().map(AdminTermsCursor::cursorField).toList();
		// 기본 보조 커서 추가
		if (!fields.contains(AdminTermsCursorField.ID)) {
			cursors.add(new AdminTermsCursor(AdminTermsCursorField.ID, Sort.Direction.DESC, null));
		}
		return cursors;
	}
}

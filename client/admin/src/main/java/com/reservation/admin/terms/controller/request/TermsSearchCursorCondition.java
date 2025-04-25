package com.reservation.admin.terms.controller.request;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;

import com.reservation.domain.terms.enums.TermsCode;

public record TermsSearchCursorCondition(
	TermsCode codeOrNull,

	Boolean isLatestOrNull,

	Integer size,

	List<TermsCursor> cursors
) {
	private static final int DEFAULT_PAGE_SIZE = 10;

	public int defaultSize() {
		return DEFAULT_PAGE_SIZE;
	}

	public List<TermsCursor> cursors() {
		List<TermsCursor> cursors =
			this.cursors == null || this.cursors.isEmpty() ? new ArrayList<>() : this.cursors;

		List<TermsCursorField> fields = cursors.stream().map(TermsCursor::cursorField).toList();

		// 기본 보조 커서 추가
		if (!fields.contains(TermsCursorField.ID)) {
			cursors.add(new TermsCursor(TermsCursorField.ID, Sort.Direction.DESC, null));
		}

		return cursors;
	}
}

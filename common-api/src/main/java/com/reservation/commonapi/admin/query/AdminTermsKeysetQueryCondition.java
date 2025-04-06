package com.reservation.commonapi.admin.query;

import static org.springframework.data.domain.Sort.Direction.*;

import java.util.ArrayList;
import java.util.List;

import com.reservation.commonapi.admin.query.sort.AdminTermsSortCursor;
import com.reservation.commonapi.admin.query.sort.AdminTermsSortField;
import com.reservation.commonmodel.terms.TermsCode;

public record AdminTermsKeysetQueryCondition(
	TermsCode code,
	boolean includeAllVersions,
	Integer size,
	List<AdminTermsSortCursor> sortCursors
) {
	public static final int DEFAULT_PAGE_SIZE = 10;

	public Integer size() {
		return size == null ? DEFAULT_PAGE_SIZE : size;
	}

	public List<AdminTermsSortCursor> sortCursors() {
		List<AdminTermsSortCursor> cursors = new ArrayList<>(
			sortCursors == null ? List.of() : sortCursors
		);
		// 보조 정렬 조건 추가
		cursors.add(new AdminTermsSortCursor(AdminTermsSortField.ID, DESC, null));
		return cursors;
	}

}

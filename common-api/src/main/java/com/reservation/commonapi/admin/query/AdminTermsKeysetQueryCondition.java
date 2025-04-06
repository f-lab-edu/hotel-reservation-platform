package com.reservation.commonapi.admin.query;

import java.util.List;

import com.reservation.commonapi.admin.query.sort.AdminTermsSortCursor;
import com.reservation.commonmodel.terms.TermsCode;

public record AdminTermsKeysetQueryCondition(
	TermsCode code,
	boolean includeAllVersions,
	Integer size,
	List<AdminTermsSortCursor> sortCursors
) {
	public static final int DEFAULT_PAGE_SIZE = 10;

	public int defaultPageSize() {
		return DEFAULT_PAGE_SIZE;
	}

}

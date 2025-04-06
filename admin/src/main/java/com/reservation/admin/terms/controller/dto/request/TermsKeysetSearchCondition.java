package com.reservation.admin.terms.controller.dto.request;

import java.util.List;

import com.reservation.commonapi.admin.query.sort.AdminTermsSortCursor;
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
	List<AdminTermsSortCursor> sortCursors
) {
	public int defaultSize() {
		return 10;
	}
}

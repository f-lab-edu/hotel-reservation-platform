package com.reservation.admin.terms.controller.dto.request;

import java.util.List;

import org.springframework.data.domain.Sort;

import com.reservation.common.request.PageableRequest;
import com.reservation.commonapi.admin.query.sort.AdminTermsSortCondition;
import com.reservation.commonapi.admin.query.sort.AdminTermsSortField;
import com.reservation.commonmodel.terms.TermsCode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TermsSearchCondition(
	@Nullable
	TermsCode code,

	@Nullable
	Boolean includeAllVersions,

	@Min(0)
	@Nullable
	Integer page,

	@Min(10) @Max(100)
	@Nullable
	Integer size,

	@Nullable
	List<AdminTermsSortCondition> sorts
) implements PageableRequest<AdminTermsSortCondition> {
	private static final int DEFAULT_PAGE_SIZE = 10;

	@Schema(hidden = true)
	@Override
	public Sort.Order getDefaultSortOrder() {
		return new Sort.Order(Sort.Direction.DESC, AdminTermsSortField.CREATED_AT.getFieldName());
	}

	@Schema(hidden = true)
	@Override
	public int defaultPageSize() {
		return DEFAULT_PAGE_SIZE;
	}
}

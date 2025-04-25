package com.reservation.admin.terms.controller.request;

import java.util.List;

import org.springframework.data.domain.Sort;

import com.reservation.domain.terms.enums.TermsCode;
import com.reservation.querysupport.page.PageableRequest;
import com.reservation.support.exception.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

public record TermsSearchCondition(
	TermsCode codeOrNull,

	Boolean isLatestOrNull,

	Integer page,

	Integer size,

	List<TermsSortCondition> sorts
) implements PageableRequest<TermsSortCondition> {
	private static final int DEFAULT_PAGE_SIZE = 10;

	public void validate() {
		if (page != null && page < 1) {
			throw ErrorCode.BAD_REQUEST.exception("페이지는 1 이상이어야 합니다.");
		}
		if (size != null && (size < 10 || size > 100)) {
			throw ErrorCode.BAD_REQUEST.exception("페이지 사이즈는 10 이상 100 이하이어야 합니다.");
		}
		if (!sorts.isEmpty()) {

		}
	}

	@Schema(hidden = true)
	@Override
	public Sort.Order getDefaultSortOrder() {
		return new Sort.Order(Sort.Direction.DESC, TermsSortField.CREATED_AT.getFieldName());
	}

	@Schema(hidden = true)
	@Override
	public int defaultPageSize() {
		return DEFAULT_PAGE_SIZE;
	}
}

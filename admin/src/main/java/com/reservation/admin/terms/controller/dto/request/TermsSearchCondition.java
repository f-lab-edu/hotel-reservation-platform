package com.reservation.admin.terms.controller.dto.request;

import static org.springframework.data.domain.Sort.*;

import java.util.List;

import com.reservation.common.request.PageableRequest;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsSortField;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TermsSearchCondition(
	@Schema(description = "약관 코드", example = "TERM_USE")
	@Nullable
	TermsCode code,

	@Schema(description = "모든 버전 포함 여부", example = "false")
	@Nullable
	Boolean includeAllVersions,

	@Schema(description = "페이지 번호 (0부터 시작)", example = "0")
	@Min(0)
	@Nullable
	Integer page,

	@Schema(description = "페이지 크기", example = "10")
	@Min(10) @Max(100)
	@Nullable
	Integer size,

	@Schema(description = "정렬 필드 목록", example = "[\"CREATED_AT\", \"EXPOSED_FROM\"]")
	@Nullable
	List<TermsSortField> sortFields,

	@Schema(description = "정렬 방향 목록", example = "[\"DESC\", \"ASC\"]")
	@Nullable
	List<Direction> sortDirections
) implements PageableRequest<TermsSortField> {
	private static final int DEFAULT_PAGE_SIZE = 10;

	@Schema(hidden = true)
	@Override
	public Order getDefaultSortOrder() {
		return new Order(Direction.DESC, TermsSortField.CREATED_AT.getFieldName());
	}

	@Schema(hidden = true)
	@Override
	public int defaultPageSize() {
		return DEFAULT_PAGE_SIZE;
	}
}

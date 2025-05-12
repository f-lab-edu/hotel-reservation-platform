package com.reservation.host.roomtype.controller.request;

import java.util.List;

import org.springframework.data.domain.Sort;

import com.reservation.querysupport.page.PageableRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RoomTypeSearchCondition(
	@Nullable
	String nameOrNull,

	@Min(0)
	@Nullable
	Integer page,

	@Min(10) @Max(100)
	@Nullable
	Integer size,

	@Nullable
	List<RoomTypeSortCondition> sorts
) implements PageableRequest<RoomTypeSortCondition> {
	private static final int DEFAULT_PAGE_SIZE = 10;

	@Schema(hidden = true)
	@Override
	public Sort.Order getDefaultSortOrder() {
		return new Sort.Order(Sort.Direction.DESC, RoomTypeSortField.NAME.getFieldName());
	}

	@Schema(hidden = true)
	@Override
	public int defaultPageSize() {
		return DEFAULT_PAGE_SIZE;
	}
}

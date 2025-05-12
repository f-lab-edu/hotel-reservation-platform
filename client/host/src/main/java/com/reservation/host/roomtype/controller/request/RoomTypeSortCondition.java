package com.reservation.host.roomtype.controller.request;

import org.springframework.data.domain.Sort;

import com.reservation.querysupport.sort.SortCondition;

import jakarta.validation.constraints.NotNull;

public record RoomTypeSortCondition(
	@NotNull RoomTypeSortField field,
	@NotNull Sort.Direction direction
) implements SortCondition<RoomTypeSortField> {
}



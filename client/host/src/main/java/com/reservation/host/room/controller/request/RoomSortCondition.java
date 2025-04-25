package com.reservation.host.room.controller.request;

import org.springframework.data.domain.Sort;

import com.reservation.querysupport.sort.SortCondition;

import jakarta.validation.constraints.NotNull;

public record RoomSortCondition(
	@NotNull RoomSortField field,
	@NotNull Sort.Direction direction
) implements SortCondition<RoomSortField> {
}



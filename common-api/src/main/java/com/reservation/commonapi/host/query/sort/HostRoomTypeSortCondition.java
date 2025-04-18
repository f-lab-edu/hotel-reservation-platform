package com.reservation.commonapi.host.query.sort;

import org.springframework.data.domain.Sort;

import com.reservation.commonmodel.sort.SortCondition;

import jakarta.annotation.Nonnull;

public record HostRoomTypeSortCondition(
	@Nonnull HostRoomTypeSortField field,
	@Nonnull Sort.Direction direction
) implements SortCondition<HostRoomTypeSortField> {
}

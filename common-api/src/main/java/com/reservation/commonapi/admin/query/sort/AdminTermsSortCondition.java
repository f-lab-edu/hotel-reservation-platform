package com.reservation.commonapi.admin.query.sort;

import org.springframework.data.domain.Sort.Direction;

import com.reservation.commonmodel.sort.SortCondition;

import jakarta.annotation.Nonnull;

public record AdminTermsSortCondition(
	@Nonnull AdminTermsSortField field,
	@Nonnull Direction direction
) implements SortCondition<AdminTermsSortField> {
}

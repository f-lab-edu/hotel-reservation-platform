package com.reservation.commonapi.admin.query.sort;

import org.springframework.data.domain.Sort.Direction;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public record AdminTermsSortCursor(
	@Nonnull AdminTermsSortField field,
	@Nonnull Direction direction,
	@Nullable String cursor
) {
}

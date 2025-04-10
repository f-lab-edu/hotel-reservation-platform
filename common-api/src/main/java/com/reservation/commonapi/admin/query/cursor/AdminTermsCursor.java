package com.reservation.commonapi.admin.query.cursor;

import org.springframework.data.domain.Sort.Direction;

import com.reservation.commonmodel.cursor.Cursor;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public record AdminTermsCursor(
	@Nonnull AdminTermsCursorField cursorField,
	@Nonnull Direction direction,
	@Nullable String value
) implements Cursor {
}

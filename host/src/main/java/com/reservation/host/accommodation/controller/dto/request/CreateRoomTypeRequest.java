package com.reservation.host.accommodation.controller.dto.request;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;

public record CreateRoomTypeRequest(
	@Nonnull @Min(1)
	Long accommodationId,
	@Nonnull
	String name,
	@Nonnull @Min(1)
	Integer capacity,
	@Nonnull @Min(1000)
	Integer price,
	@Nullable
	String description,
	@Nonnull @Min(0)
	Integer roomCount
) {
}

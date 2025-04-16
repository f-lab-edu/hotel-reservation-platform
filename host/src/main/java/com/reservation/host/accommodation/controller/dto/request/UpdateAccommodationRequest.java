package com.reservation.host.accommodation.controller.dto.request;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateAccommodationRequest(
	@Nonnull @Min(1L)
	Long id,

	@Nonnull @Min(1L)
	Long hostId,

	@Nonnull
	String name,

	@Nullable @Max(1000)
	String descriptionOrNull,

	@Nonnull
	String location,

	@Nonnull
	Boolean isVisible,

	@Nullable
	String mainImageUrlOrNull,

	@Nonnull
	String contactNumber) {
}

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
	String address,

	@Nonnull @Min(-90) @Max(90)
	Double latitude,

	@Nonnull @Min(-180) @Max(180)
	Double longitude,

	@Nonnull
	Boolean isVisible,

	@Nullable
	String mainImageUrlOrNull,

	@Nonnull
	String contactNumber) {
}

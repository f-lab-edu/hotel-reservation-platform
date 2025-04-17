package com.reservation.host.accommodation.controller.dto.request;

import java.util.List;

import org.hibernate.validator.constraints.URL;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;

public record UpdateRoomImagesRequest(
	@Nonnull @Min(1)
	Long roomTypeId,
	@Nonnull
	List<UpdateRoomImage> roomImages
) {
	public record UpdateRoomImage(
		@Nullable @Min(1)
		Long id,
		@Nonnull @URL
		String imageUrl,
		@Nonnull @Min(1)
		Integer displayOrder,
		@Nonnull
		Boolean isMainImage
	) {
	}
}

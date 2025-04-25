package com.reservation.host.accommodation.service.dto;

import com.reservation.domain.accommodation.Location;

public record DefaultAccommodationInfo(
	String name,
	String descriptionOrNull,
	Location location,
	Boolean isVisible,
	String mainImageUrlOrNull,
	String contactNumber
) {
}

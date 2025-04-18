package com.reservation.commonmodel.accommodation;

import com.reservation.commonmodel.host.HostDto;

public record AccommodationDto(
	Long id,
	HostDto host,
	String name,
	String descriptionOrNull,
	LocationDto location,
	Boolean isVisible,
	String mainImageUrlOrNull,
	String contactNumber
) {
}

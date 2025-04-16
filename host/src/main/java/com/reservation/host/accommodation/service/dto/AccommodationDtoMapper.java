package com.reservation.host.accommodation.service.dto;

import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.LocationDto;
import com.reservation.commonmodel.host.HostDto;
import com.reservation.host.accommodation.controller.dto.request.CreateAccommodationRequest;
import com.reservation.host.accommodation.controller.dto.request.UpdateAccommodationRequest;

public class AccommodationDtoMapper {
	public static AccommodationDto fromCreateAccommodationRequest(CreateAccommodationRequest request, HostDto host) {
		LocationDto locationDto = new LocationDto(request.address(), request.latitude(), request.longitude());
		return new AccommodationDto(
			null,
			host,
			request.name(),
			request.descriptionOrNull(),
			locationDto,
			request.isVisible(),
			request.mainImageUrlOrNull(),
			request.contactNumber()
		);
	}

	public static AccommodationDto fromUpdateAccommodationRequest(UpdateAccommodationRequest request, HostDto host) {
		LocationDto locationDto = new LocationDto(request.address(), request.latitude(), request.longitude());

		return new AccommodationDto(
			request.id(),
			host,
			request.name(),
			request.descriptionOrNull(),
			locationDto,
			request.isVisible(),
			request.mainImageUrlOrNull(),
			request.contactNumber()
		);
	}
}

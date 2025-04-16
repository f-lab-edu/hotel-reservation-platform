package com.reservation.common.accommodation.repository.mapper;

import com.reservation.common.accommodation.domain.Accommodation;
import com.reservation.common.accommodation.domain.Location;
import com.reservation.common.host.domain.Host;
import com.reservation.common.host.repository.dto.HostDtoMapper;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.LocationDto;
import com.reservation.commonmodel.host.HostDto;

public class AccommodationDtoMapper {
	public static AccommodationDto fromAccommodation(Accommodation accommodation) {
		HostDto hostDto = HostDtoMapper.fromHost(accommodation.getHost());
		LocationDto locationDto = new LocationDto(
			accommodation.getLocation().getAddress(),
			accommodation.getLocation().getLatitude(),
			accommodation.getLocation().getLongitude()
		);

		return new AccommodationDto(
			accommodation.getId(),
			hostDto,
			accommodation.getName(),
			accommodation.getDescriptionOrNull(),
			locationDto,
			accommodation.getIsVisible(),
			accommodation.getMainImageUrlOrNull(),
			accommodation.getContactNumber()
		);
	}

	public static Accommodation toAccommodation(AccommodationDto accommodationDto) {
		Host host = HostDtoMapper.toHost(accommodationDto.host());
		LocationDto locationDto = accommodationDto.location();
		Location location = new Location(locationDto.address(), locationDto.latitude(), locationDto.longitude());

		return new Accommodation.AccommodationBuilder()
			.id(accommodationDto.id())
			.host(host)
			.name(accommodationDto.name())
			.descriptionOrNull(accommodationDto.descriptionOrNull())
			.location(location)
			.isVisible(accommodationDto.isVisible())
			.mainImageUrlOrNull(accommodationDto.mainImageUrlOrNull())
			.contactNumber(accommodationDto.contactNumber())
			.build();
	}
}

package com.reservation.common.accommodation.repository.mapper;

import com.reservation.common.accommodation.domain.Accommodation;
import com.reservation.common.host.domain.Host;
import com.reservation.common.host.repository.dto.HostDtoMapper;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.host.HostDto;

public class AccommodationDtoMapper {
	public static AccommodationDto fromAccommodation(Accommodation accommodation) {
		HostDto hostDto = HostDtoMapper.fromHost(accommodation.getHost());

		return new AccommodationDto(
			accommodation.getId(),
			hostDto,
			accommodation.getName(),
			accommodation.getDescriptionOrNull(),
			accommodation.getLocation(),
			accommodation.getIsVisible(),
			accommodation.getMainImageUrlOrNull(),
			accommodation.getContactNumber()
		);
	}

	public static Accommodation toAccommodation(AccommodationDto accommodationDto) {
		Host host = HostDtoMapper.toHost(accommodationDto.host());

		return new Accommodation.AccommodationBuilder()
			.id(accommodationDto.id())
			.host(host)
			.name(accommodationDto.name())
			.descriptionOrNull(accommodationDto.descriptionOrNull())
			.location(accommodationDto.location())
			.isVisible(accommodationDto.isVisible())
			.mainImageUrlOrNull(accommodationDto.mainImageUrlOrNull())
			.contactNumber(accommodationDto.contactNumber())
			.build();
	}
}

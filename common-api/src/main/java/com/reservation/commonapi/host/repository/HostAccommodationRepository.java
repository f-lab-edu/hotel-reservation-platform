package com.reservation.commonapi.host.repository;

import java.util.Optional;

import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.LocationDto;

public interface HostAccommodationRepository {
	Optional<AccommodationDto> findOneByNameAndLocation(String name, LocationDto location);

	Boolean existsByHostId(Long hostId);

	Boolean existsByNameAndLocation(String name, LocationDto location);

	AccommodationDto save(AccommodationDto accommodationDto);

	Boolean existsByIdAndHostId(Long id, Long hostId);

	Optional<AccommodationDto> findByHostId(Long hostId);
}

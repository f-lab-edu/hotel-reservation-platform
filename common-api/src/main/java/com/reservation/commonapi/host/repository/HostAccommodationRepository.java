package com.reservation.commonapi.host.repository;

import java.util.Optional;

import com.reservation.commonmodel.accommodation.AccommodationDto;

public interface HostAccommodationRepository {
	Optional<AccommodationDto> findOneByNameAndLocation(String name, String location);

	Boolean existsByHostId(Long hostId);

	Boolean existsByNameAndLocation(String name, String location);

	AccommodationDto save(AccommodationDto accommodationDto);

	Boolean existsByIdAndHostId(Long id, Long hostId);

	Optional<AccommodationDto> findByHostId(Long hostId);
}

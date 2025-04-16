package com.reservation.common.accommodation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.common.accommodation.domain.Accommodation;
import com.reservation.common.accommodation.domain.Location;

public interface JpaAccommodationRepository extends JpaRepository<Accommodation, Long> {
	Boolean existsByHostId(Long hostId);

	Boolean existsByNameAndLocation(String name, Location location);

	Boolean existsByIdAndHostId(long id, Long hostId);

	Optional<Accommodation> findOneByNameAndLocation(String name, Location location);

	Optional<Accommodation> findOneByHostId(Long hostId);
}

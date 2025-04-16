package com.reservation.common.accommodation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.common.accommodation.domain.Accommodation;

public interface JpaAccommodationRepository extends JpaRepository<Accommodation, Long> {
	Boolean existsByHostId(Long hostId);

	Boolean existsByNameAndLocation(String name, String location);

	Boolean existsByIdAndHostId(long id, Long hostId);

	Optional<Accommodation> findOneByNameAndLocation(String name, String location);

	Optional<Accommodation> findOneByHostId(Long hostId);
}

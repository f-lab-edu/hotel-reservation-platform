package com.reservation.common.accommodation.repository;

import static com.reservation.common.accommodation.repository.mapper.AccommodationDtoMapper.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.reservation.common.accommodation.domain.Accommodation;
import com.reservation.common.accommodation.repository.mapper.AccommodationDtoMapper;
import com.reservation.commonapi.host.repository.HostAccommodationRepository;
import com.reservation.commonmodel.accommodation.AccommodationDto;

@Repository
public class AccommodationRepository implements HostAccommodationRepository {
	private final JpaAccommodationRepository jpaAccommodationRepository;

	public AccommodationRepository(JpaAccommodationRepository jpaAccommodationRepository) {
		this.jpaAccommodationRepository = jpaAccommodationRepository;
	}

	@Override
	public Optional<AccommodationDto> findOneByNameAndLocation(String name, String location) {
		return jpaAccommodationRepository.findOneByNameAndLocation(name, location)
			.map(AccommodationDtoMapper::fromAccommodation);
	}

	@Override
	public Boolean existsByHostId(Long hostId) {
		return jpaAccommodationRepository.existsByHostId(hostId);
	}

	@Override
	public Boolean existsByNameAndLocation(String name, String location) {
		return jpaAccommodationRepository.existsByNameAndLocation(name, location);
	}

	@Override
	public AccommodationDto save(AccommodationDto accommodationDto) {
		Accommodation accommodation = toAccommodation(accommodationDto);
		return fromAccommodation(jpaAccommodationRepository.save(accommodation));
	}

	@Override
	public Boolean existsByIdAndHostId(Long id, Long hostId) {
		return jpaAccommodationRepository.existsByIdAndHostId(id, hostId);
	}

	@Override
	public Optional<AccommodationDto> findByHostId(Long hostId) {
		return jpaAccommodationRepository.findOneByHostId(hostId).map(AccommodationDtoMapper::fromAccommodation);
	}
}

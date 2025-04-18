package com.reservation.common.accommodation.repository;

import static com.reservation.common.accommodation.repository.mapper.AccommodationMapper.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.reservation.common.accommodation.domain.Accommodation;
import com.reservation.common.accommodation.domain.Location;
import com.reservation.common.accommodation.repository.mapper.AccommodationMapper;
import com.reservation.commonapi.host.repository.HostAccommodationRepository;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.LocationDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AccommodationRepository implements HostAccommodationRepository {
	private final JpaAccommodationRepository jpaAccommodationRepository;

	@Override
	public Optional<AccommodationDto> findOneByNameAndLocation(String name, LocationDto locationDto) {
		Location location = new Location(locationDto.address(), locationDto.latitude(), locationDto.longitude());
		return jpaAccommodationRepository.findOneByNameAndLocation(name, location)
			.map(AccommodationMapper::fromEntityToDto);
	}

	@Override
	public Boolean existsByHostId(Long hostId) {
		return jpaAccommodationRepository.existsByHostId(hostId);
	}

	@Override
	public Boolean existsByNameAndLocation(String name, LocationDto locationDto) {
		Location location = new Location(locationDto.address(), locationDto.latitude(), locationDto.longitude());
		return jpaAccommodationRepository.existsByNameAndLocation(name, location);
	}

	@Override
	public AccommodationDto save(AccommodationDto accommodationDto) {
		Accommodation entity = fromDtoToEntity(accommodationDto);
		return fromEntityToDto(jpaAccommodationRepository.save(entity));
	}

	@Override
	public Boolean existsByIdAndHostId(Long id, Long hostId) {
		return jpaAccommodationRepository.existsByIdAndHostId(id, hostId);
	}

	@Override
	public Optional<AccommodationDto> findByHostId(Long hostId) {
		return jpaAccommodationRepository.findOneByHostId(hostId).map(AccommodationMapper::fromEntityToDto);
	}
}

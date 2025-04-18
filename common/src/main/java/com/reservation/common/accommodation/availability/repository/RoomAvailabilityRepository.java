package com.reservation.common.accommodation.availability.repository;

import static com.reservation.common.accommodation.availability.repository.mapper.RoomAvailabilityMapper.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.reservation.common.accommodation.availability.domain.RoomAvailability;
import com.reservation.common.accommodation.availability.repository.mapper.RoomAvailabilityMapper;
import com.reservation.commonapi.host.query.HostRoomAvailabilityQueryCondition;
import com.reservation.commonapi.host.repository.HostRoomAvailabilityRepository;
import com.reservation.commonmodel.accommodation.RoomAvailabilityDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoomAvailabilityRepository implements HostRoomAvailabilityRepository {
	private final JpaRoomAvailabilityRepository jpaRoomAvailabilityRepository;

	@Override
	public RoomAvailabilityDto save(RoomAvailabilityDto roomAvailabilityDto) {
		RoomAvailability roomAvailability = fromDtoToEntity(roomAvailabilityDto);

		return fromEntityToDto(jpaRoomAvailabilityRepository.save(roomAvailability));
	}

	@Override
	public List<RoomAvailabilityDto> findRoomAvailability(HostRoomAvailabilityQueryCondition queryCondition) {
		return jpaRoomAvailabilityRepository.findByRoomTypeIdAndDateBetween(
				queryCondition.roomTypeId(), queryCondition.startDate(), queryCondition.endDate())
			.stream()
			.map(RoomAvailabilityMapper::fromEntityToDto)
			.toList();
	}
}

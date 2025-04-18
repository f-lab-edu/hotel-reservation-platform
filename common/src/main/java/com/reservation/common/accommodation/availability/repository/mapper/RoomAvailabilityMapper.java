package com.reservation.common.accommodation.availability.repository.mapper;

import com.reservation.common.accommodation.availability.domain.RoomAvailability;
import com.reservation.commonmodel.accommodation.RoomAvailabilityDto;

public class RoomAvailabilityMapper {
	public static RoomAvailabilityDto fromEntityToDto(RoomAvailability roomAvailability) {
		return new RoomAvailabilityDto(
			roomAvailability.getId(),
			roomAvailability.getRoomTypeId(),
			roomAvailability.getDate(),
			roomAvailability.getAvailableCount()
		);
	}

	public static RoomAvailability fromDtoToEntity(RoomAvailabilityDto roomAvailabilityDto) {
		return new RoomAvailability(
			roomAvailabilityDto.id(),
			roomAvailabilityDto.roomTypeId(),
			roomAvailabilityDto.date(),
			roomAvailabilityDto.availableCount()
		);
	}
}

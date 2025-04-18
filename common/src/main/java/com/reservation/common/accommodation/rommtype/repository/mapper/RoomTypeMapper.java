package com.reservation.common.accommodation.rommtype.repository.mapper;

import com.reservation.common.accommodation.rommtype.domain.RoomType;
import com.reservation.commonmodel.accommodation.RoomTypeDto;

public class RoomTypeMapper {
	public static RoomTypeDto fromEntityToDto(RoomType fromEntity) {
		return new RoomTypeDto(
			fromEntity.getId(),
			fromEntity.getAccommodationId(),
			fromEntity.getName(),
			fromEntity.getCapacity(),
			fromEntity.getPrice(),
			fromEntity.getDescriptionOrNull(),
			fromEntity.getRoomCount()
		);
	}

	public static RoomType fromDtoToEntity(RoomTypeDto fromDto) {
		return new RoomType(
			fromDto.id(),
			fromDto.accommodationId(),
			fromDto.name(),
			fromDto.capacity(),
			fromDto.price(),
			fromDto.descriptionOrNull(),
			fromDto.roomCount()
		);
	}
}

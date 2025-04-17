package com.reservation.common.accommodation.repository.mapper;

import com.reservation.common.accommodation.domain.RoomImage;
import com.reservation.commonmodel.accommodation.RoomImageDto;

public class RoomImageMapper {
	public static RoomImageDto fromEntityToDto(RoomImage roomImage) {
		return new RoomImageDto(
			roomImage.getId(),
			roomImage.getRoomTypeId(),
			roomImage.getImageUrl(),
			roomImage.getDisplayOrder(),
			roomImage.getIsMainImage()
		);
	}

	public static RoomImage fromDtoToEntity(RoomImageDto roomImageDto) {
		return new RoomImage(
			roomImageDto.id(),
			roomImageDto.roomTypeId(),
			roomImageDto.imageUrl(),
			roomImageDto.displayOrder(),
			roomImageDto.isMainImage()
		);
	}
}

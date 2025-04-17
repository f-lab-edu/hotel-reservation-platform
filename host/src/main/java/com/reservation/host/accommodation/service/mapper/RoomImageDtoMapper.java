package com.reservation.host.accommodation.service.mapper;

import com.reservation.commonmodel.accommodation.RoomImageDto;
import com.reservation.host.accommodation.controller.dto.request.UpdateRoomImagesRequest;

public class RoomImageDtoMapper {
	public static RoomImageDto fromUpdateRoomImage(UpdateRoomImagesRequest.UpdateRoomImage roomImageDto,
		Long roomTypeId) {
		return new RoomImageDto(
			roomImageDto.id(),
			roomTypeId,
			roomImageDto.imageUrl(),
			roomImageDto.displayOrder(),
			roomImageDto.isMainImage()
		);
	}
}

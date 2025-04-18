package com.reservation.host.accommodation.roomimage.service.mapper;

import com.reservation.commonmodel.accommodation.RoomImageDto;
import com.reservation.host.accommodation.roomimage.controller.dto.request.UpdateRoomImagesRequest;

public class RoomImageDtoMapper {
	public static RoomImageDto fromUpdateRoomImage(UpdateRoomImagesRequest.UpdateRoomImage roomImageDto,
		Long roomTypeId, String uploadUrl) {
		return new RoomImageDto(
			roomImageDto.id(),
			roomTypeId,
			uploadUrl,
			roomImageDto.displayOrder(),
			roomImageDto.isMainImage()
		);
	}
}

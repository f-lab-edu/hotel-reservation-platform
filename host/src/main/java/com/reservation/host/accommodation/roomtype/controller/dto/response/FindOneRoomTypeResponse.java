package com.reservation.host.accommodation.roomtype.controller.dto.response;

import java.util.List;

import com.reservation.commonmodel.accommodation.RoomImageDto;
import com.reservation.commonmodel.accommodation.RoomTypeDto;

public record FindOneRoomTypeResponse(
	RoomTypeDto roomType,
	List<RoomImageDto> roomImages
) {
}

package com.reservation.host.accommodation.service.mapper;

import com.reservation.commonapi.host.query.HostRoomTypeQueryCondition;
import com.reservation.commonmodel.accommodation.RoomTypeDto;
import com.reservation.host.accommodation.controller.dto.request.CreateRoomTypeRequest;
import com.reservation.host.accommodation.controller.dto.request.RoomTypeSearchCondition;
import com.reservation.host.accommodation.controller.dto.request.UpdateRoomTypeRequest;

public class RoomTypeDtoMapper {
	public static RoomTypeDto fromCreateRequest(CreateRoomTypeRequest request) {
		return new RoomTypeDto(
			null,
			request.accommodationId(),
			request.name(),
			request.capacity(),
			request.price(),
			request.description(),
			request.roomCount()
		);
	}

	public static RoomTypeDto fromUpdateRequest(UpdateRoomTypeRequest request) {
		return new RoomTypeDto(
			request.id(),
			request.accommodationId(),
			request.name(),
			request.capacity(),
			request.price(),
			request.description(),
			request.roomCount()
		);
	}

	public static HostRoomTypeQueryCondition fromSearchConditionToQueryCondition(RoomTypeSearchCondition condition) {
		return new HostRoomTypeQueryCondition(
			condition.name(),
			condition.toPageRequest()
		);
	}
}

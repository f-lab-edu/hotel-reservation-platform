package com.reservation.host.accommodation.availability.service.mapper;

import com.reservation.commonapi.host.query.HostRoomAvailabilityQueryCondition;
import com.reservation.commonmodel.accommodation.RoomAvailabilityDto;
import com.reservation.host.accommodation.availability.controller.dto.request.CreateRoomAvailabilityRequest;
import com.reservation.host.accommodation.availability.controller.dto.request.RoomAvailabilitySearchCondition;
import com.reservation.host.accommodation.availability.controller.dto.request.UpdateRoomAvailabilityRequest;

public class RoomAvailabilityDtoMapper {
	public static RoomAvailabilityDto fromCreateRequestToDto(CreateRoomAvailabilityRequest request) {
		return new RoomAvailabilityDto(
			null,
			request.roomTypeId(),
			request.date(),
			request.availableCount()
		);
	}

	public static RoomAvailabilityDto fromUpdateRequestToDto(UpdateRoomAvailabilityRequest request) {
		return new RoomAvailabilityDto(
			request.id(),
			request.roomTypeId(),
			request.date(),
			request.availableCount()
		);
	}

	public static HostRoomAvailabilityQueryCondition fromSearchConditionToQueryCondition(
		RoomAvailabilitySearchCondition condition) {
		return new HostRoomAvailabilityQueryCondition(
			condition.roomTypeId(),
			condition.startDate(),
			condition.endDate()
		);
	}
}

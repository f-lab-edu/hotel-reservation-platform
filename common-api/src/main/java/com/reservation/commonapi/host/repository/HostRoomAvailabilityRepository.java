package com.reservation.commonapi.host.repository;

import java.util.List;

import com.reservation.commonapi.host.query.HostRoomAvailabilityQueryCondition;
import com.reservation.commonmodel.accommodation.RoomAvailabilityDto;

public interface HostRoomAvailabilityRepository {
	RoomAvailabilityDto save(RoomAvailabilityDto roomAvailabilityDto);

	List<RoomAvailabilityDto> findRoomAvailability(HostRoomAvailabilityQueryCondition queryCondition);
}

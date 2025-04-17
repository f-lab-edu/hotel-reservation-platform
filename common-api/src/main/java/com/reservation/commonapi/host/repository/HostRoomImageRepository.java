package com.reservation.commonapi.host.repository;

import java.util.List;

import com.reservation.commonmodel.accommodation.RoomImageDto;

public interface HostRoomImageRepository {
	List<RoomImageDto> findByRoomTypeId(Long roomTypeId);

	void deleteAllById(List<Long> deletedRoomImageIds);

	void saveAll(List<RoomImageDto> newRoomImages);
}

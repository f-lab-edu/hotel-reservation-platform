package com.reservation.commonapi.host.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.reservation.commonapi.host.query.HostRoomTypeQueryCondition;
import com.reservation.commonapi.host.repository.dto.HostRoomTypeDto;
import com.reservation.commonmodel.accommodation.RoomTypeDto;

public interface HostRoomTypeRepository {
	boolean existsByNameAndAccommodationId(String name, Long accommodationId);

	RoomTypeDto save(RoomTypeDto roomTypeDto);

	boolean existsByIdAndAccommodationId(Long id, Long accommodationId);

	Optional<RoomTypeDto> findOneByNameAndAccommodationId(String name, Long accommodationId);

	Page<HostRoomTypeDto> findRoomTypes(Long accommodationId, HostRoomTypeQueryCondition condition);

	Optional<RoomTypeDto> findOneByIdAndAccommodationId(Long id, Long accommodationId);
}

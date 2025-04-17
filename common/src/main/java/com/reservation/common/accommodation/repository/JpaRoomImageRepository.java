package com.reservation.common.accommodation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.common.accommodation.domain.RoomImage;

public interface JpaRoomImageRepository extends JpaRepository<RoomImage, Long> {
	List<RoomImage> findByRoomTypeId(Long roomTypeId);
}

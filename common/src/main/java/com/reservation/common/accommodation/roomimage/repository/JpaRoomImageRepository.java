package com.reservation.common.accommodation.roomimage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.common.accommodation.roomimage.domain.RoomImage;

public interface JpaRoomImageRepository extends JpaRepository<RoomImage, Long> {
	List<RoomImage> findByRoomTypeId(Long roomTypeId);
}

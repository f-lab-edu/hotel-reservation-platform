package com.reservation.host.roomimage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roomimage.RoomImage;

public interface JpaRoomImageRepository extends JpaRepository<RoomImage, Long> {
	List<RoomImage> findByRoomTypeId(Long roomTypeId);
}

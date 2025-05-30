package com.reservation.batch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roomtype.RoomType;

public interface JpaRoomTypeRepository extends JpaRepository<RoomType, Long> {
	List<RoomType> findByIdIn(List<Long> ids);
}

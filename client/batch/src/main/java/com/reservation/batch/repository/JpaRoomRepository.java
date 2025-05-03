package com.reservation.batch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.room.Room;

public interface JpaRoomRepository extends JpaRepository<Room, Long> {
	List<Room> findByIdIn(List<Long> ids);
}

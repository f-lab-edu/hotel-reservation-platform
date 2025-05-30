package com.reservation.customer.roomtype.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roomtype.RoomType;

public interface JpaRoomTypeRepository extends JpaRepository<RoomType, Long> {
}

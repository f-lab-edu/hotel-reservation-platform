package com.reservation.host.roomavailability.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roomavailability.RoomAvailability;

public interface JpaRoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
	List<RoomAvailability> findByRoomTypeIdAndDateBetween(Long roomTypeId, LocalDate startDate, LocalDate endDate);
}

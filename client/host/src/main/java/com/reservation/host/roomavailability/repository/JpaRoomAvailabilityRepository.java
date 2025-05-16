package com.reservation.host.roomavailability.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roomavailability.OriginRoomAvailability;

public interface JpaRoomAvailabilityRepository extends JpaRepository<OriginRoomAvailability, Long> {
	List<OriginRoomAvailability> findByRoomTypeIdAndOpenDateBetween(
		Long roomTypeId, LocalDate startDate, LocalDate endDate);
}

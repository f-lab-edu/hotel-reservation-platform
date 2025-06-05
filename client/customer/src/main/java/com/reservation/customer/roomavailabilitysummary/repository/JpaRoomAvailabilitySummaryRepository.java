package com.reservation.customer.roomavailabilitysummary.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roomavailabilitysummary.RoomAvailabilitySummary;

public interface JpaRoomAvailabilitySummaryRepository extends JpaRepository<RoomAvailabilitySummary, Long> {
	Optional<RoomAvailabilitySummary> findOneByRoomTypeIdAndCheckInDate(Long roomTypeId, LocalDate checkInDate);
}

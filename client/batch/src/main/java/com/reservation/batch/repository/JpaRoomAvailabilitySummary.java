package com.reservation.batch.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult;
import com.reservation.domain.roomavailabilitysummary.RoomAvailabilitySummary;

public interface JpaRoomAvailabilitySummary extends JpaRepository<RoomAvailabilitySummary, Long> {

	@Query(
		"SELECT new com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult(ra.roomTypeId, ra.checkInDate) "
			+
			"FROM RoomAvailabilitySummary ra " +
			"WHERE ra.roomTypeId IN :roomTypeIds " +
			"AND ra.checkInDate BETWEEN :startDate AND :endDate")
	List<FindAvailabilityInRoomIdsResult> findExistingDatesByRoomIds(
		@Param("roomTypeIds") List<Long> roomTypeIds,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate);
}

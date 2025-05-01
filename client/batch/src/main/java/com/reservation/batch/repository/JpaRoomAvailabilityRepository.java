package com.reservation.batch.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult;
import com.reservation.domain.roomavailability.RoomAvailability;

public interface JpaRoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
	@Query("SELECT new com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult(ra.roomId, ra.date)  " +
		"FROM RoomAvailability ra " +
		"WHERE ra.roomId IN :roomIds " +
		"AND ra.date BETWEEN :startDate AND :endDate")
	List<FindAvailabilityInRoomIdsResult> findExistingDatesByRoomIds(
		@Param("roomIds") List<Long> roomIds,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate);
}

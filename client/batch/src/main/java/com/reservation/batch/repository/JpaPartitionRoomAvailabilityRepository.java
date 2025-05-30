package com.reservation.batch.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult;
import com.reservation.domain.roomavailability.PartitionRoomAvailability;
import com.reservation.domain.roomavailability.RoomAvailabilityId;

public interface JpaPartitionRoomAvailabilityRepository
	extends JpaRepository<PartitionRoomAvailability, RoomAvailabilityId> {

	@Query(
		"SELECT new com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult(ra.id.roomTypeId, ra.id.openDate) "
			+
			"FROM PartitionRoomAvailability ra " +
			"WHERE ra.id.roomTypeId IN :roomTypeIds " +
			"AND ra.id.openDate BETWEEN :startDate AND :endDate")
	List<FindAvailabilityInRoomIdsResult> findExistingDatesByRoomIds(
		@Param("roomTypeIds") List<Long> roomTypeIds,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate);
}


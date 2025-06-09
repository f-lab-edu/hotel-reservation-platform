package com.reservation.batch.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult;
import com.reservation.domain.roomavailability.OriginRoomAvailability;
import com.reservation.domain.roomavailability.RoomAvailabilityId;

public interface JpaOriginRoomAvailabilityRepository extends JpaRepository<OriginRoomAvailability, RoomAvailabilityId> {
	@Query(
		"SELECT new com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult(ra.roomTypeId, ra.openDate) " +
			"FROM OriginRoomAvailability ra " +
			"WHERE ra.roomTypeId IN :roomTypeIds " +
			"AND ra.openDate BETWEEN :startDate AND :endDate")
	List<FindAvailabilityInRoomIdsResult> findExistingDatesByRoomIds(
		@Param("roomTypeIds") List<Long> roomTypeIds,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate);

	@Query("SELECT ra " +
		"FROM OriginRoomAvailability ra " +
		"WHERE ra.roomTypeId = :roomTypeId " +
		"AND ra.openDate BETWEEN :startDate AND :endDate")
	List<OriginRoomAvailability> findExistingDatesByRoomId(
		@Param("roomTypeId") Long roomTypeId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate);
}

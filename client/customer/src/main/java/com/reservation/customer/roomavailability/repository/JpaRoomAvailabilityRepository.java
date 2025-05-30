package com.reservation.customer.roomavailability.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reservation.domain.roomavailability.OriginRoomAvailability;

public interface JpaRoomAvailabilityRepository extends JpaRepository<OriginRoomAvailability, Long> {
	long countByRoomTypeIdAndOpenDateBetweenAndAvailableCountGreaterThanEqual(
		Long roomTypeId, LocalDate startDate, LocalDate endDate, int availableCount
	);

	@Query("""
		    SELECT COALESCE(SUM(o.price), 0)
		    FROM OriginRoomAvailability o
		    WHERE o.roomTypeId = :roomTypeId
		    AND o.openDate BETWEEN :startDate AND :endDate
		""")
	int sumPriceByRoomTypeIdAndDateRange(
		@Param("roomTypeId") Long roomTypeId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	List<OriginRoomAvailability> findAllByRoomTypeIdAndOpenDateBetween(
		Long roomTypeId, LocalDate startDate, LocalDate endDate
	);
}


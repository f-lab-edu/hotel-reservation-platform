package com.reservation.customer.roomavailabilitysummary.repository;

import java.time.LocalDate;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoomAvailabilitySummaryRepository {
	private final EntityManager entityManager;

	public int findRemainAvailabilityCount(
		Long roomTypeId,
		LocalDate checkIn,
		int capacity,
		long requiredDayCount
	) {
		String mainSql = """
			SELECT s.available_count%d
			FROM room_availability_summary s
			JOIN room_type rt ON s.room_type_id = rt.id
			WHERE s.room_type_id = :roomTypeId
			  AND s.check_in_date = :checkIn
			  AND rt.capacity >= :capacity
			""".formatted(requiredDayCount);

		return (int)entityManager.createNativeQuery(mainSql)
			.setParameter("roomTypeId", roomTypeId)
			.setParameter("checkIn", checkIn)
			.setParameter("capacity", capacity)
			.getSingleResult();
	}
}

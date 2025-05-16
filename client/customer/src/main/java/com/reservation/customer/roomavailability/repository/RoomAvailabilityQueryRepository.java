package com.reservation.customer.roomavailability.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.reservation.customer.roomavailability.repository.dto.SearchAvailableRoomSortField;
import com.reservation.domain.accommodation.RoomAvailabilitySearchResult;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoomAvailabilityQueryRepository {
	private final EntityManager entityManager;

	public PageImpl<RoomAvailabilitySearchResult> searchRoomAvailability(
		LocalDate checkIn,
		LocalDate checkOut,
		long requiredDayCount,
		int capacity,
		SearchAvailableRoomSortField sortField,
		int page,
		int size
	) {
		int offset = page * size;

		// 1. 정렬 구문
		String orderClause = switch (sortField) {
			case SearchAvailableRoomSortField.LOWEST_PRICE -> "ORDER BY average_price DESC";
			default -> "ORDER BY average_price ASC";
		};

		// 2. 본문 쿼리
		String mainSql = """
			SELECT accommodation_id, accommodation_name, average_price
			FROM (
			    SELECT 
			        acc.id AS accommodation_id,
			        acc.name AS accommodation_name,
			        FLOOR(SUM(ra.price) / :requiredDayCount) AS average_price,
			        ROW_NUMBER() OVER (
			            PARTITION BY acc.id 
			            ORDER BY SUM(ra.price) / :requiredDayCount ASC
			        ) AS row_num
			    FROM room_availability ra
			    JOIN room_type rt ON ra.room_type_id = rt.id
			    JOIN accommodation acc ON rt.accommodation_id = acc.id
			    WHERE ra.open_date >= :checkIn AND ra.open_date < :checkOut
			      AND ra.available_count > 0
			      AND rt.capacity >= :capacity
			    GROUP BY rt.id, acc.id, acc.name
			    HAVING COUNT(DISTINCT ra.open_date) = :requiredDayCount
			) t
			WHERE row_num = 1
			""" + orderClause + " LIMIT :limit OFFSET :offset";

		// 3. 개수 쿼리
		String countSql = """
			SELECT COUNT(*) FROM (
			    SELECT acc.id
			    FROM room_availability ra
			    JOIN room_type rt ON ra.room_type_id = rt.id
			    JOIN accommodation acc ON rt.accommodation_id = acc.id
			    WHERE ra.open_date >= :checkIn AND ra.open_date < :checkOut
			      AND ra.available_count > 0
			      AND rt.capacity >= :capacity
			    GROUP BY rt.id, acc.id
			    HAVING COUNT(DISTINCT ra.open_date) = :requiredDayCount
			) t
			""";

		// 4. 결과 실행
		List<RoomAvailabilitySearchResult> results =
			entityManager.createNativeQuery(mainSql, "RoomAvailabilitySearchResultMapping")
				.setParameter("checkIn", checkIn)
				.setParameter("checkOut", checkOut)
				.setParameter("requiredDayCount", requiredDayCount)
				.setParameter("capacity", capacity)
				.setParameter("limit", size)
				.setParameter("offset", offset)
				.getResultList();

		Long totalCount = ((Number)entityManager.createNativeQuery(countSql)
			.setParameter("checkIn", checkIn)
			.setParameter("checkOut", checkOut)
			.setParameter("requiredDayCount", requiredDayCount)
			.setParameter("capacity", capacity)
			.getSingleResult()).longValue();

		// 5. PageImpl 생성
		return new PageImpl<>(results, PageRequest.of(page, size), totalCount);
	}
}

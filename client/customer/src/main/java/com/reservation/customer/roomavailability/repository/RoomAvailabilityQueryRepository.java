package com.reservation.customer.roomavailability.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.reservation.customer.roomavailability.repository.dto.AvailableRoomTypeResult;
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
		long requiredDayCount,
		int capacity,
		SearchAvailableRoomSortField sortField,
		int page,
		int size
	) {
		int offset = page * size;

		// 1. 정렬 구문
		String orderClause = switch (sortField) {
			case SearchAvailableRoomSortField.LOWEST_PRICE ->
				"ORDER BY total_price%d DESC, room_type_id ASC".formatted(requiredDayCount);
			default -> "ORDER BY total_price%d ASC, room_type_id ASC".formatted(requiredDayCount);
		};

		// 2. 본문 쿼리
		String mainSql = """
			SELECT
			    s.room_type_id,
			    rt.accommodation_id,
			    acc.name AS accommodation_name,
			    s.total_price%d AS total_price
			FROM room_availability_summary s
			JOIN room_type rt ON s.room_type_id = rt.id
			JOIN accommodation acc ON rt.accommodation_id = acc.id
			WHERE s.check_in_date = :checkIn
			  AND s.available_count%d > 0
			  AND rt.capacity >= :capacity
			""".formatted(requiredDayCount, requiredDayCount)
			+ orderClause + " LIMIT :limit OFFSET :offset";

		// 3. 개수 쿼리
		String countSql = """
			SELECT COUNT(*)
			FROM room_availability_summary s
			JOIN room_type rt ON s.room_type_id = rt.id
			WHERE s.check_in_date = :checkIn
			  AND s.available_count%d > 0
			  AND rt.capacity >= :capacity
			""".formatted(requiredDayCount);

		// 4. 결과 실행
		List<RoomAvailabilitySearchResult> results =
			entityManager.createNativeQuery(mainSql, "RoomAvailabilitySearchResultMapping")
				.setParameter("checkIn", checkIn)
				.setParameter("capacity", capacity)
				.setParameter("limit", size)
				.setParameter("offset", offset)
				.getResultList();

		long totalCount = ((Number)entityManager.createNativeQuery(countSql)
			.setParameter("checkIn", checkIn)
			.setParameter("capacity", capacity)
			.getSingleResult()).longValue();

		// 5. PageImpl 생성
		return new PageImpl<>(results, PageRequest.of(page, size), totalCount);
	}

	public List<AvailableRoomTypeResult> findAvailableRoomTypes(
		Long accommodationId,
		LocalDate checkIn,
		int capacity,
		long requiredDayCount
	) {
		String mainSql = """
			SELECT
			    s.room_type_id,
			    rt.name,
			    rt.capacity,
			    s.total_price%d AS total_price,
			    ri.image_url,
			    s.available_count%d
			FROM room_availability_summary s
			JOIN room_type rt ON s.room_type_id = rt.id
			JOIN accommodation acc ON rt.accommodation_id = acc.id
			LEFT OUTER JOIN room_image ri ON ri.room_type_id = rt.id AND ri.is_main_image = true
			WHERE rt.accommodation_id = :accommodationId
			  AND s.check_in_date = :checkIn
			  AND s.available_count%d > 0
			  AND rt.capacity >= :capacity
			""".formatted(requiredDayCount, requiredDayCount, requiredDayCount);

		List<Object[]> rows = entityManager.createNativeQuery(mainSql)
			.setParameter("accommodationId", accommodationId)
			.setParameter("checkIn", checkIn)
			.setParameter("capacity", capacity)
			.getResultList();

		return rows.stream()
			.map(row -> new AvailableRoomTypeResult(
				((Number)row[0]).longValue(),
				(String)row[1],
				((Number)row[2]).intValue(),
				((Number)row[3]).intValue(),
				(String)row[4],
				((Number)row[5]).intValue()
			))
			.toList();
	}
}

package com.reservation.batch.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.reservation.batch.repository.dto.CursorPage;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoomAvailabilityRepository {
	private final EntityManager em;

	public CursorPage<RoomAutoAvailabilityPolicy, Long> fetchNextPage(Long lastSeenId, int pageSize) {
		if (lastSeenId == null) {
			lastSeenId = 0L;
		}

		List<RoomAutoAvailabilityPolicy> results = em.createQuery(
				"SELECT p FROM RoomAutoAvailabilityPolicy p " +
					"WHERE p.enabled = true AND p.id > :lastSeenId " +
					"ORDER BY p.id ASC", RoomAutoAvailabilityPolicy.class)
			.setParameter("lastSeenId", lastSeenId)
			.setMaxResults(pageSize + 1)
			.getResultList();

		boolean hasNext = results.size() > pageSize;
		Long nextCursor = null;

		if (hasNext) {
			RoomAutoAvailabilityPolicy last = results.remove(pageSize); // 마지막은 nextCursor 용으로 뺀다
			nextCursor = last.getId();
		}

		return new CursorPage<>(results, nextCursor);
	}
}

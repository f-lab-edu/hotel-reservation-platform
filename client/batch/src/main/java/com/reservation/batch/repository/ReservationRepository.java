package com.reservation.batch.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.reservation.batch.repository.dto.Cursor;
import com.reservation.domain.reservation.Reservation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReservationRepository {
	private static final long INITIAL_ID = 0L;
	private static final int PAGE_SIZE = 1;
	private static final int CURSOR_SIZE = PAGE_SIZE + 1; // 페이지 사이즈 + nextCursor 용

	private final EntityManager em;

	public Cursor<Reservation, Long> fetchOneNext(Long lastSeenId) {
		if (lastSeenId == null) {
			lastSeenId = INITIAL_ID;
		}

		LocalDateTime beforeTenMinutes = LocalDateTime.now().minusMinutes(10L);

		List<Reservation> results = em.createQuery(
				"SELECT r FROM Reservation r " +
					"WHERE r.status = 'PENDING' AND r.id >= :lastSeenId AND r.createdAt <= :beforeTenMinutes " +
					"ORDER BY r.id ASC", Reservation.class)
			.setParameter("lastSeenId", lastSeenId)
			.setParameter("beforeTenMinutes", beforeTenMinutes)
			.setMaxResults(CURSOR_SIZE)
			.getResultList();

		if (results.isEmpty()) {
			return null;
		}

		Long nextCursor = null;

		boolean hasNext = results.size() == CURSOR_SIZE;
		if (hasNext) {
			Reservation last = results.remove(PAGE_SIZE); // 마지막은 nextCursor 용으로 뺀다
			nextCursor = last.getId();
		}

		return new Cursor<>(results.getFirst(), nextCursor);
	}
}

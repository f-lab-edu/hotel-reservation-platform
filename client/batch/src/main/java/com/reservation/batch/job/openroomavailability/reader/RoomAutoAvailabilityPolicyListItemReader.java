package com.reservation.batch.job.openroomavailability.reader;

import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import com.reservation.batch.repository.RoomAvailabilityRepository;
import com.reservation.batch.repository.dto.CursorPage;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class RoomAutoAvailabilityPolicyListItemReader implements ItemReader<List<RoomAutoAvailabilityPolicy>> {
	private static final int READ_SIZE = 100;

	private final RoomAvailabilityRepository roomAvailabilityRepository;

	private Long lastSeenId = null;
	private boolean finished = false;

	@Override
	public List<RoomAutoAvailabilityPolicy> read() {
		if (finished) {
			return null;
		}

		CursorPage<RoomAutoAvailabilityPolicy, Long> cursorPage =
			roomAvailabilityRepository.fetchNextPage(lastSeenId, READ_SIZE);

		if (cursorPage.content().isEmpty()) {
			finished = true;
			return null;
		}
		lastSeenId = cursorPage.nextCursor();

		return cursorPage.content();
	}
}

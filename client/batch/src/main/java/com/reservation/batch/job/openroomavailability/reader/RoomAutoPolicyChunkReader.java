package com.reservation.batch.job.openroomavailability.reader;

import java.util.List;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.reservation.batch.repository.RoomAvailabilityRepository;
import com.reservation.batch.repository.dto.CursorPage;
import com.reservation.batch.utils.Perf;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class RoomAutoPolicyChunkReader implements ItemReader<List<RoomAutoAvailabilityPolicy>> {
	private static final int READ_SIZE = 500;

	private final RoomAvailabilityRepository availabilityRepository;

	private Long lastSeenId = null;
	private boolean finished = false;

	@Value("#{stepExecution.jobExecution}")
	private JobExecution jobExecution;

	@Override
	public List<RoomAutoAvailabilityPolicy> read() {
		if (jobExecution.isStopping()) {
			log.error("Job execution stopped {}", jobExecution.getExitStatus().getExitDescription());
			throw ErrorCode.CONFLICT.exception("Job 중단 요청됨 → Reader 중단");
		}
		if (finished) {
			return null;
		}

		Perf perf = new Perf();

		CursorPage<RoomAutoAvailabilityPolicy, Long> cursorPage =
			availabilityRepository.fetchNextPage(lastSeenId, READ_SIZE);

		lastSeenId = cursorPage.nextCursor();

		if (!cursorPage.hasNext()) {
			finished = true;
		}

		perf.log("Reader rows", cursorPage.content().size());
		return cursorPage.content();
	}
}

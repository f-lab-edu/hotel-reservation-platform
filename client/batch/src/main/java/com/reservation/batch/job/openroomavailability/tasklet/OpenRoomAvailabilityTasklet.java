package com.reservation.batch.job.openroomavailability.tasklet;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.reservation.batch.job.openroomavailability.processor.RoomAutoAvailabilityPolicyListProcessor;
import com.reservation.batch.job.openroomavailability.reader.RoomAutoAvailabilityPolicyListItemReader;
import com.reservation.batch.job.openroomavailability.writer.RoomAvailabilityBatchWriter;
import com.reservation.batch.repository.dto.CursorPage;
import com.reservation.batch.utils.Perf;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.domain.roomavailability.RoomAvailability;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class OpenRoomAvailabilityTasklet implements Tasklet {
	private static final long TIMEOUT_MILLIS = 5000L;
	private static final int MAX_WRITE_COUNT = 100000;

	private final RoomAutoAvailabilityPolicyListItemReader roomAutoAvailabilityPolicyListItemReader;
	private final RoomAutoAvailabilityPolicyListProcessor roomAutoAvailabilityPolicyListProcessor;
	private final RoomAvailabilityBatchWriter roomAvailabilityBatchWriter;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		long executeStartTime = System.currentTimeMillis();
		boolean readProcessorWriterSet = false;
		List<RoomAvailability> writeRoomAvailabilities = new ArrayList<>();

		Long lastSeenId = chunkContext.getAttribute("lastSeenId") != null
			? (Long)chunkContext.getAttribute("lastSeenId")
			: null;
		boolean hasNext = true;
		Perf perf = new Perf();

		while (!readProcessorWriterSet) {
			CursorPage<RoomAutoAvailabilityPolicy, Long> roomAutoAvailabilityPolicyLongCursorPage =
				roomAutoAvailabilityPolicyListItemReader.read(lastSeenId);
			contribution.incrementReadCount();

			List<RoomAutoAvailabilityPolicy> readRoomAutoAvailabilityPolicies =
				roomAutoAvailabilityPolicyLongCursorPage.content();
			lastSeenId = roomAutoAvailabilityPolicyLongCursorPage.nextCursor();
			hasNext = roomAutoAvailabilityPolicyLongCursorPage.hasNext();
			perf.log("readerRows", readRoomAutoAvailabilityPolicies.size());

			List<RoomAvailability> roomAvailabilities =
				roomAutoAvailabilityPolicyListProcessor.process(readRoomAutoAvailabilityPolicies);
			contribution.incrementFilterCount(1L);
			perf.log("emittedRows", roomAvailabilities.size());

			writeRoomAvailabilities.addAll(roomAvailabilities);

			if (!hasNext) {
				break;
			}
			readProcessorWriterSet = System.currentTimeMillis() - executeStartTime > TIMEOUT_MILLIS
				|| writeRoomAvailabilities.size() > MAX_WRITE_COUNT;
		}

		roomAvailabilityBatchWriter.write(writeRoomAvailabilities);
		contribution.incrementWriteCount(1L);
		perf.log("writeRows", writeRoomAvailabilities.size());

		if (hasNext) {
			chunkContext.setAttribute("lastSeenId", lastSeenId);
			return RepeatStatus.CONTINUABLE;
		}

		return RepeatStatus.FINISHED;
	}
}

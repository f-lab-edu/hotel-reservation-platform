package com.reservation.batch.job.openroomavailability.taskletStep.tasklet;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.reservation.batch.job.openroomavailability.taskletStep.processor.OriginOpenAvailabilityTaskletProcessor;
import com.reservation.batch.job.openroomavailability.taskletStep.reader.RoomAutoPolicyTaskletReader;
import com.reservation.batch.job.openroomavailability.taskletStep.writer.RoomAvailabilityTaskletWriter;
import com.reservation.batch.repository.dto.CursorPage;
import com.reservation.batch.utils.Perf;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.domain.roomavailability.OriginRoomAvailability;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class OriginOpenAvailabilityTasklet implements Tasklet {
	private static final int BASE_LINE_WRITE_COUNT = 90000;
	private static final int MAX_WRITE_COUNT = 150000;

	private final RoomAutoPolicyTaskletReader autoPolicyReader;
	private final OriginOpenAvailabilityTaskletProcessor openAvailabilityProcessor;
	private final RoomAvailabilityTaskletWriter availabilityWriter;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		// 성능 측정을 위한 시간 로깅
		Perf perf = new Perf();

		// execute 과정에서 기록된 다음 reader 시작점
		Long lastSeenId = getLastSeenId(chunkContext.getAttribute("lastSeenId"));

		// Output 임계치까지 [read - process] 반복 실행
		ReadProcessCombineResult combineResult = combineReaderProcessor(lastSeenId, contribution, perf);

		// Output 결과 저장
		writeAvailabilities(combineResult.outputAvailabilities, contribution, perf);

		return handleExecuteResult(combineResult.hasNext, combineResult.lastSeenId, chunkContext);
	}

	private Long getLastSeenId(Object lastSeenId) {
		if (lastSeenId == null) {
			return null;
		}
		if (lastSeenId instanceof Long) {
			return (Long)lastSeenId;
		}
		throw ErrorCode.CONFLICT.exception("Invalid lastSeenId type: " + lastSeenId.getClass().getName());
	}

	private record ReadProcessCombineResult(
		boolean hasNext,
		Long lastSeenId,
		List<OriginRoomAvailability> outputAvailabilities
	) {
	}

	// Output 임계치까지 [read - processor] 반복 수행
	private ReadProcessCombineResult combineReaderProcessor(
		Long lastSeenId,
		StepContribution contribution,
		Perf perf
	) {
		boolean outputThreshold = false;
		boolean hasNext = true;
		List<OriginRoomAvailability> outputResult = new ArrayList<>(MAX_WRITE_COUNT);

		while (!outputThreshold) {
			CursorPage<RoomAutoAvailabilityPolicy, Long> autoPolicyCursorPage = autoPolicyReader.read(lastSeenId);
			contribution.incrementReadCount();

			List<RoomAutoAvailabilityPolicy> inputAutoPolicies = autoPolicyCursorPage.content();
			perf.log("Reader rows", inputAutoPolicies.size());

			lastSeenId = autoPolicyCursorPage.nextCursor();

			List<OriginRoomAvailability> outputAvailabilities = openAvailabilityProcessor.process(inputAutoPolicies);
			perf.log("Output rows", outputAvailabilities.size());

			outputResult.addAll(outputAvailabilities);

			if (!autoPolicyCursorPage.hasNext()) {
				hasNext = false;
				break;
			}
			outputThreshold = outputResult.size() >= BASE_LINE_WRITE_COUNT;
		}

		return new ReadProcessCombineResult(hasNext, lastSeenId, outputResult);
	}

	private void writeAvailabilities(
		List<OriginRoomAvailability> writeAvailabilities,
		StepContribution contribution,
		Perf perf
	) {
		availabilityWriter.write(writeAvailabilities);
		contribution.incrementWriteCount(writeAvailabilities.size());
		perf.log("Write rows", writeAvailabilities.size());
	}

	private RepeatStatus handleExecuteResult(
		boolean hasNext,
		Long lastSeenId,
		ChunkContext chunkContext
	) {
		if (hasNext) {
			chunkContext.setAttribute("lastSeenId", lastSeenId);
			return RepeatStatus.CONTINUABLE;
		}
		return RepeatStatus.FINISHED;
	}
}

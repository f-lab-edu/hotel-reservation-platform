package com.reservation.batch.job.openroomavailability.tasklet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.reservation.batch.job.openroomavailability.processor.ImprovedOpenAvailabilityTaskletProcessor;
import com.reservation.batch.job.openroomavailability.reader.RoomAutoPolicyTaskletReader;
import com.reservation.batch.job.openroomavailability.writer.RoomAvailabilityTaskletWriter;
import com.reservation.batch.repository.dto.CursorPage;
import com.reservation.batch.utils.Perf;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.domain.roomavailability.RoomAvailability;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class ImprovedOpenAvailabilityTasklet implements Tasklet {
	private static final int BASE_LINE_WRITE_COUNT = 100000;
	private static final int MAX_WRITE_COUNT = 150000;

	private final RoomAutoPolicyTaskletReader autoPolicyReader;
	private final ImprovedOpenAvailabilityTaskletProcessor openAvailabilityProcessor;
	private final RoomAvailabilityTaskletWriter availabilityWriter;

	private List<RoomAvailability> remainAvailabilities = new ArrayList<>();

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		// 성능 측정을 위한 시간 로깅
		Perf perf = new Perf();

		// execute 과정에서 기록된 다음 reader 시작점
		Long lastSeenId = getLastSeenId(chunkContext.getAttribute("lastSeenId"));

		// Output 임계치까지 [read - process] 반복 실행
		ReadProcessCombineResult combineResult = combineReaderProcessor(lastSeenId, contribution, perf);

		// Output 결과 저장
		writeAvailabilities(combineResult.outputAvailabilities, combineResult.hasNext, contribution, perf);

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
		List<RoomAvailability> outputAvailabilities
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
		List<RoomAvailability> outputResult = new ArrayList<>(MAX_WRITE_COUNT);

		while (!outputThreshold) {
			CursorPage<RoomAutoAvailabilityPolicy, Long> autoPolicyCursorPage = autoPolicyReader.read(lastSeenId);
			contribution.incrementReadCount();

			List<RoomAutoAvailabilityPolicy> inputAutoPolicies = autoPolicyCursorPage.content();
			perf.log("Reader rows", inputAutoPolicies.size());

			lastSeenId = autoPolicyCursorPage.nextCursor();

			List<RoomAvailability> outputAvailabilities = openAvailabilityProcessor.process(inputAutoPolicies);
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
		List<RoomAvailability> writeAvailabilities,
		boolean hasNext,
		StepContribution contribution,
		Perf perf
	) {
		remainAvailabilities.addAll(writeAvailabilities);
		int tryWriterCount = remainAvailabilities.size() / BASE_LINE_WRITE_COUNT;
		IntStream.range(0, tryWriterCount)
			.parallel()
			.forEach(i -> {
				int startIndex = i * BASE_LINE_WRITE_COUNT;
				int endIndex = startIndex + BASE_LINE_WRITE_COUNT;
				List<RoomAvailability> writeChunk = remainAvailabilities.subList(startIndex, endIndex);
				availabilityWriter.write(writeChunk);
				contribution.incrementWriteCount(writeChunk.size());
				perf.log("Write rows", writeChunk.size());
			});

		remainAvailabilities =
			remainAvailabilities.subList(tryWriterCount * BASE_LINE_WRITE_COUNT, remainAvailabilities.size());

		if (!hasNext) {
			availabilityWriter.write(remainAvailabilities);
			contribution.incrementWriteCount(remainAvailabilities.size());
			perf.log("Last Write rows", remainAvailabilities.size());
		}
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

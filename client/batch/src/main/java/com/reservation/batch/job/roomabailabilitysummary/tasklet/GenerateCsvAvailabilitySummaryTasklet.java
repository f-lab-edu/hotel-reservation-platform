package com.reservation.batch.job.roomabailabilitysummary.tasklet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.reservation.batch.job.roomabailabilitysummary.processor.CsvRoomAvailabilitySummaryProcessor;
import com.reservation.batch.job.roomabailabilitysummary.reader.RoomAutoAvailabilityPolicyReader;
import com.reservation.batch.job.roomabailabilitysummary.writer.GenerateCsvRoomAvailabilitySummaryWriter;
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
public class GenerateCsvAvailabilitySummaryTasklet implements Tasklet {
	private static final int BASE_LINE_WRITE_COUNT = 300000;
	private static final int MAX_WRITE_COUNT = 400000;
	private static final String PREFIX_FILE_NAME = "availability";
	private static final String FILE_EXTENSION = ".csv";
	public static final String STEP_CSV_ATTRIBUTE_NAME = "csvFilePaths";

	private final RoomAutoAvailabilityPolicyReader reader;
	private final CsvRoomAvailabilitySummaryProcessor processor;
	private final GenerateCsvRoomAvailabilitySummaryWriter writer;

	private final Path basePath = createDirectories();
	private final Set<String> csvFilePaths = new HashSet<>();

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		// 성능 측정을 위한 시간 로깅
		Perf perf = new Perf();

		// execute 과정에서 기록된 다음 reader 시작점
		Long lastSeenId = getLastSeenId(chunkContext.getAttribute("lastSeenId"));

		// Output 임계치까지 [read - process] 반복 실행
		ReadProcessCombineResult combineResult = combineReaderProcessor(lastSeenId, contribution, perf);

		// Output 결과 CSV 파일로 저장
		generateCsvAvailabilities(combineResult.outputAvailabilitySummaries, perf);

		return handleExecuteResult(combineResult.hasNext, combineResult.lastSeenId, contribution, chunkContext);
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

	// Output 임계치까지 [read - processor] 반복 수행
	private ReadProcessCombineResult combineReaderProcessor(
		Long lastSeenId,
		StepContribution contribution,
		Perf perf
	) {
		boolean outputThreshold = false;
		boolean hasNext = true;
		List<String[]> outputResult = new ArrayList<>(MAX_WRITE_COUNT);

		while (!outputThreshold) {
			CursorPage<RoomAutoAvailabilityPolicy, Long> autoPolicyCursorPage = reader.read(lastSeenId);
			contribution.incrementReadCount();

			List<RoomAutoAvailabilityPolicy> inputAutoPolicies = autoPolicyCursorPage.content();
			perf.log("Reader rows", inputAutoPolicies.size());

			lastSeenId = autoPolicyCursorPage.nextCursor();

			List<String[]> outputAvailabilitySummaries = processor.process(inputAutoPolicies);
			perf.log("Output rows", outputAvailabilitySummaries.size());

			outputResult.addAll(outputAvailabilitySummaries);

			if (!autoPolicyCursorPage.hasNext()) {
				hasNext = false;
				break;
			}
			outputThreshold = outputResult.size() >= BASE_LINE_WRITE_COUNT;
		}

		return new ReadProcessCombineResult(hasNext, lastSeenId, outputResult);
	}

	private record ReadProcessCombineResult(
		boolean hasNext,
		Long lastSeenId,
		List<String[]> outputAvailabilitySummaries
	) {
	}

	private void generateCsvAvailabilities(
		List<String[]> writeAvailabilities,
		Perf perf
	) {
		if (writeAvailabilities.isEmpty()) {
			return;
		}

		Map<YearMonth, List<String[]>> groupedByMonth = writeAvailabilities.stream()
			.collect(Collectors.groupingBy(avail -> {
				String date = avail[1];
				return YearMonth.from(LocalDate.parse(date));
			}));
		List<YearMonth> yearMonths = new ArrayList<>(groupedByMonth.keySet());
		IntStream.range(0, groupedByMonth.size())
			.parallel()
			.forEach(i -> {
				List<String[]> writeChunk = groupedByMonth.get(yearMonths.get(i));

				String fileName = PREFIX_FILE_NAME + yearMonths.get(i) + FILE_EXTENSION;
				Path filePath = Path.of(basePath.toString(), fileName);

				writer.write(writeChunk, filePath);
				perf.log(fileName + " Write rows", writeChunk.size());

				csvFilePaths.add(filePath.toAbsolutePath().toString());
			});
	}

	// csv 파일을 저장할 디렉토리 생성
	private Path createDirectories() {
		LocalDateTime now = LocalDateTime.now();

		// 디렉토리 형식: 2025-05-14_20:21
		String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm"));

		// 최종 디렉토리 경로: tmp/batch/summary/2025-05-14_20:21
		Path dirPath = Path.of("tmp", "batch", "summary", timestamp);
		try {
			return Files.createDirectories(dirPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private RepeatStatus handleExecuteResult(
		boolean hasNext,
		Long lastSeenId,
		StepContribution contribution,
		ChunkContext chunkContext
	) {
		if (hasNext) {
			chunkContext.setAttribute("lastSeenId", lastSeenId);
			return RepeatStatus.CONTINUABLE;
		}

		contribution.getStepExecution()
			.getJobExecution()
			.getExecutionContext()
			.put(STEP_CSV_ATTRIBUTE_NAME, csvFilePaths);

		return RepeatStatus.FINISHED;
	}
}

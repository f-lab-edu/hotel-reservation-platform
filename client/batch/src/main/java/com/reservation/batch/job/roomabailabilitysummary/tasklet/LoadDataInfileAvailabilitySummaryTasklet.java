package com.reservation.batch.job.roomabailabilitysummary.tasklet;

import static com.reservation.batch.job.openroomavailability.loadinfile.tasklet.GenerateCsvOpenAvailabilityTasklet.*;

import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.reservation.batch.utils.Perf;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class LoadDataInfileAvailabilitySummaryTasklet implements Tasklet {
	private final JdbcTemplate jdbcTemplate;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		Set<String> csvFilePaths = getCsvFilePaths(contribution);
		String[] csvFilePathArray = csvFilePaths.toArray(new String[0]);

		Perf perf = new Perf();
		IntStream.range(0, csvFilePathArray.length)
			.parallel()
			.forEach(i -> {
				String sql = "LOAD DATA LOCAL INFILE '" + csvFilePathArray[i] + "' " +
					"IGNORE " +
					"INTO TABLE room_availability_summary " +
					"FIELDS TERMINATED BY ',' " +
					"LINES TERMINATED BY '\\n' " +
					"(room_type_id, check_in_date, available_count1, available_count2, available_count3, " +
					"available_count4, available_count5, available_count6, available_count7, available_count8, " +
					"available_count9, available_count10, available_count11, available_count12, available_count13, " +
					"available_count14, available_count15, available_count16, available_count17, available_count18, " +
					"available_count19, available_count20, available_count21, available_count22, available_count23, " +
					"available_count24, available_count25, available_count26, available_count27, available_count28, " +
					"available_count29, available_count30, total_price1, total_price2, total_price3, total_price4, " +
					"total_price5, total_price6, total_price7, total_price8, total_price9, total_price10, total_price11 ,"
					+ "total_price12, total_price13, total_price14, total_price15, total_price16, total_price17, total_price18, "
					+ "total_price19, total_price20, total_price21, total_price22, total_price23, total_price24, total_price25, "
					+ "total_price26, total_price27, total_price28, total_price29, total_price30) " +
					"SET created_at = NOW(), updated_at = NOW() ";

				jdbcTemplate.execute(sql);
				perf.log("CSV file loaded: " + csvFilePathArray[i], 0);
			});

		return RepeatStatus.FINISHED;
	}

	private Set<String> getCsvFilePaths(StepContribution contribution) {
		StepExecution stepExecution = contribution.getStepExecution();
		JobExecution jobExecution = stepExecution.getJobExecution();
		ExecutionContext executionContext = jobExecution.getExecutionContext();
		Object csvFilePathsObj = executionContext.get(STEP_CSV_ATTRIBUTE_NAME);

		if (!(csvFilePathsObj instanceof Set)) {
			throw ErrorCode.CONFLICT.exception("csvFilePaths is not a List");
		}

		return (Set<String>)csvFilePathsObj;
	}
}

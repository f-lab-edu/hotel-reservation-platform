package com.reservation.batch.job.openroomavailability.loadinfile.tasklet;

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
public class LoadDataInfileOpenAvailabilityTasklet implements Tasklet {
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
					"INTO TABLE csv_room_availability " +
					"FIELDS TERMINATED BY ',' " +
					"LINES TERMINATED BY '\\n' " +
					"(room_type_id, open_date, available_count, price) " +
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

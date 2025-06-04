package com.reservation.batch.job.roomabailabilitysummary;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.reservation.batch.job.roomabailabilitysummary.tasklet.GenerateCsvAvailabilitySummaryTasklet;
import com.reservation.batch.job.roomabailabilitysummary.tasklet.LoadDataInfileAvailabilitySummaryTasklet;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RoomAvailabilitySummaryJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Job roomAvailabilitySummaryJob(
		Step generateCsvAvailabilitySummaryStep,
		Step loadCsvAvailabilitySummaryStep
	) {
		String jobName = "roomAvailabilitySummaryJob";

		return new JobBuilder(jobName, jobRepository)
			.start(generateCsvAvailabilitySummaryStep)
			.next(loadCsvAvailabilitySummaryStep)
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step generateCsvAvailabilitySummaryStep(
		GenerateCsvAvailabilitySummaryTasklet generateCsvAvailabilitySummaryTasklet
	) {
		String stepName = "generateCsvAvailabilitySummaryStep";

		return new StepBuilder(stepName, jobRepository)
			.tasklet(generateCsvAvailabilitySummaryTasklet, transactionManager)
			.build();
	}

	@Bean
	public Step loadCsvAvailabilitySummaryStep(
		LoadDataInfileAvailabilitySummaryTasklet loadDataInfileAvailabilitySummaryTasklet
	) {
		String stepName = "loadCsvAvailabilitySummaryStep";

		return new StepBuilder(stepName, jobRepository)
			.tasklet(loadDataInfileAvailabilitySummaryTasklet, transactionManager)
			.build();
	}
}

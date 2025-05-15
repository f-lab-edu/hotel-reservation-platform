package com.reservation.batch.job.openroomavailability.loadinfile;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.reservation.batch.job.openroomavailability.loadinfile.tasklet.GenerateCsvOpenAvailabilityTasklet;
import com.reservation.batch.job.openroomavailability.loadinfile.tasklet.LoadDataInfileOpenAvailabilityTasklet;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class InfileOpenRoomJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Job infileOpenAvailabilityTaskletJob(
		Step generateCsvOpenAvailabilityStep,
		Step loadCsvOpenAvailabilityStep
	) {
		String jobName = "infileOpenAvailabilityTaskletJob";

		return new JobBuilder(jobName, jobRepository)
			.start(generateCsvOpenAvailabilityStep)
			.next(loadCsvOpenAvailabilityStep)
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step generateCsvOpenAvailabilityStep(GenerateCsvOpenAvailabilityTasklet generateCsvOpenAvailabilityTasklet) {
		String stepName = "generateCsvOpenAvailabilityStep";

		return new StepBuilder(stepName, jobRepository)
			.tasklet(generateCsvOpenAvailabilityTasklet, transactionManager)
			.build();
	}

	@Bean
	public Step loadCsvOpenAvailabilityStep(
		LoadDataInfileOpenAvailabilityTasklet loadDataInfileOpenAvailabilityTasklet) {
		String stepName = "loadCsvOpenAvailabilityStep";

		return new StepBuilder(stepName, jobRepository)
			.tasklet(loadDataInfileOpenAvailabilityTasklet, transactionManager)
			.build();
	}
}

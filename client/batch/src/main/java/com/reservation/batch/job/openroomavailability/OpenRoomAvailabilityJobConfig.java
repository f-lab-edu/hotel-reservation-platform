package com.reservation.batch.job.openroomavailability;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.reservation.batch.job.openroomavailability.tasklet.OpenRoomAvailabilityTasklet;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class OpenRoomAvailabilityJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Job openRoomAvailabilityJob(Step openRoomAvailabilityStep) {
		String jobName = "openRoomAvailabilityJob";

		return new JobBuilder(jobName, jobRepository)
			.start(openRoomAvailabilityStep)
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step openRoomAvailabilityStep(OpenRoomAvailabilityTasklet openRoomAvailabilityTasklet) {
		String stepName = "openRoomAvailabilityStep";

		return new StepBuilder(stepName, jobRepository)
			.tasklet(openRoomAvailabilityTasklet, transactionManager)
			.build();
	}
}

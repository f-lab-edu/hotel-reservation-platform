package com.reservation.batch.job.openroomavailability.taskletStep;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.reservation.batch.job.openroomavailability.taskletStep.tasklet.ImprovedOpenAvailabilityTasklet;
import com.reservation.batch.job.openroomavailability.taskletStep.tasklet.OriginOpenAvailabilityTasklet;
import com.reservation.batch.job.openroomavailability.taskletStep.tasklet.PartitionOpenAvailabilityTasklet;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class TaskletOpenRoomJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Job originOpenAvailabilityTaskletJob(Step originOpenAvailabilityTaskletStep) {
		String jobName = "originOpenAvailabilityTaskletJob";

		return new JobBuilder(jobName, jobRepository)
			.start(originOpenAvailabilityTaskletStep)
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step originOpenAvailabilityTaskletStep(OriginOpenAvailabilityTasklet originTasklet) {
		String stepName = "openRoomAvailabilityTaskletStep";

		return new StepBuilder(stepName, jobRepository)
			.tasklet(originTasklet, transactionManager)
			.build();
	}

	@Bean
	public Job improvedOpenAvailabilityTaskletJob(Step improvedOpenAvailabilityTaskletStep) {
		String jobName = "improvedOpenAvailabilityTaskletJob";

		return new JobBuilder(jobName, jobRepository)
			.start(improvedOpenAvailabilityTaskletStep)
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step improvedOpenAvailabilityTaskletStep(ImprovedOpenAvailabilityTasklet improvedTasklet) {
		String stepName = "improvedOpenAvailabilityTaskletStep";

		return new StepBuilder(stepName, jobRepository)
			.tasklet(improvedTasklet, transactionManager)
			.build();
	}

	@Bean
	public Job partitionOpenAvailabilityTaskletJob(Step partitionOpenAvailabilityTaskletStep) {
		String jobName = "partitionOpenAvailabilityTaskletJob";

		return new JobBuilder(jobName, jobRepository)
			.start(partitionOpenAvailabilityTaskletStep)
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step partitionOpenAvailabilityTaskletStep(PartitionOpenAvailabilityTasklet partitionTasklet) {
		String stepName = "partitionOpenAvailabilityTaskletStep";

		return new StepBuilder(stepName, jobRepository)
			.tasklet(partitionTasklet, transactionManager)
			.build();
	}
}

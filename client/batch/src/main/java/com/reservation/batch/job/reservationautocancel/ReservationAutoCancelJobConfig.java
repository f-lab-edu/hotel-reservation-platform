package com.reservation.batch.job.reservationautocancel;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.reservation.batch.job.reservationautocancel.tasklet.ReservationAutoCancelTasklet;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ReservationAutoCancelJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Job reservationAutoCancelJob(Step reservationAutoCancelStep) {
		String jobName = "reservationAutoCancelJob";

		return new JobBuilder(jobName, jobRepository)
			.start(reservationAutoCancelStep)
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step reservationAutoCancelStep(ReservationAutoCancelTasklet reservationAutoCancelTasklet) {
		String stepName = "reservationAutoCancelStep";

		return new StepBuilder(stepName, jobRepository)
			.tasklet(reservationAutoCancelTasklet)
			.build();
	}
}

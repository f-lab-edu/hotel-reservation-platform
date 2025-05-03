package com.reservation.batch.job.openroomavailability;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.reservation.batch.job.openroomavailability.processor.RoomAutoAvailabilityPolicyListItemProcessor;
import com.reservation.batch.job.openroomavailability.reader.RoomAutoAvailabilityPolicyListItemReader;
import com.reservation.batch.job.openroomavailability.tasklet.OpenRoomAvailabilityTasklet;
import com.reservation.batch.job.openroomavailability.writer.RoomAvailabilityBatchItemWriter;
import com.reservation.batch.step.policy.TimeAndSizeBasedCompletionPolicy;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.domain.roomavailability.RoomAvailability;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class OpenRoomAvailabilityJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Job openRoomAvailabilityChunkJob(Step openRoomAvailabilityChunkStep) {
		String jobName = "openRoomAvailabilityChunkJob";

		return new JobBuilder(jobName, jobRepository)
			.start(openRoomAvailabilityChunkStep)
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step openRoomAvailabilityChunkStep(
		RoomAutoAvailabilityPolicyListItemReader roomAutoAvailabilityPolicyListItemReader,
		RoomAutoAvailabilityPolicyListItemProcessor roomAutoAvailabilityPolicyListItemProcessor,
		RoomAvailabilityBatchItemWriter roomAvailabilityBatchItemWriter
	) {
		String stepName = "openRoomAvailabilityChunkStep";

		TimeAndSizeBasedCompletionPolicy timeAndSizeBasedCompletionPolicy =
			new TimeAndSizeBasedCompletionPolicy(5, 10);

		return new StepBuilder(stepName, jobRepository)
			.<List<RoomAutoAvailabilityPolicy>, List<RoomAvailability>>
				chunk(timeAndSizeBasedCompletionPolicy, transactionManager)
			.reader(roomAutoAvailabilityPolicyListItemReader)
			.processor(roomAutoAvailabilityPolicyListItemProcessor)
			.writer(roomAvailabilityBatchItemWriter)
			.build();
	}

	@Bean
	public Job openRoomAvailabilityTaskletJob(Step openRoomAvailabilityTaskletStep) {
		String jobName = "openRoomAvailabilityTaskletJob";

		return new JobBuilder(jobName, jobRepository)
			.start(openRoomAvailabilityTaskletStep)
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step openRoomAvailabilityTaskletStep(OpenRoomAvailabilityTasklet openRoomAvailabilityTasklet) {
		String stepName = "openRoomAvailabilityTaskletStep";

		return new StepBuilder(stepName, jobRepository)
			.tasklet(openRoomAvailabilityTasklet, transactionManager)
			.build();
	}
}

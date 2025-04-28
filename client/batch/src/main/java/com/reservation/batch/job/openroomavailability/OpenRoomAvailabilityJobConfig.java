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

import com.reservation.batch.job.openroomavailability.processor.RoomAutoAvailabilityPolicyListProcessor;
import com.reservation.batch.job.openroomavailability.reader.RoomAutoAvailabilityPolicyListItemReader;
import com.reservation.batch.job.openroomavailability.writer.RoomAvailabilityBatchWriter;
import com.reservation.batch.policy.TimeAndSizeBasedCompletionPolicy;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.domain.roomavailability.RoomAvailability;

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
	public Step openRoomAvailabilityStep(
		RoomAutoAvailabilityPolicyListItemReader roomAutoAvailabilityPolicyListItemReader,
		RoomAutoAvailabilityPolicyListProcessor roomAutoAvailabilityPolicyListProcessor,
		RoomAvailabilityBatchWriter roomAvailabilityBatchWriter
	) {
		String stepName = "openRoomAvailabilityStep";

		TimeAndSizeBasedCompletionPolicy timeAndSizeBasedCompletionPolicy =
			new TimeAndSizeBasedCompletionPolicy(5, 100000);

		return new StepBuilder(stepName, jobRepository)
			.<List<RoomAutoAvailabilityPolicy>, List<RoomAvailability>>
				chunk(timeAndSizeBasedCompletionPolicy, transactionManager)
			.reader(roomAutoAvailabilityPolicyListItemReader)
			.processor(roomAutoAvailabilityPolicyListProcessor)
			.writer(roomAvailabilityBatchWriter)
			.build();
	}
}

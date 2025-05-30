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

import com.reservation.batch.job.openroomavailability.processor.ImprovedOpenAvailabilityChunkProcessor;
import com.reservation.batch.job.openroomavailability.processor.OriginOpenAvailabilityChunkProcessor;
import com.reservation.batch.job.openroomavailability.reader.RoomAutoPolicyChunkReader;
import com.reservation.batch.job.openroomavailability.tasklet.ImprovedOpenAvailabilityTasklet;
import com.reservation.batch.job.openroomavailability.tasklet.OriginOpenAvailabilityTasklet;
import com.reservation.batch.job.openroomavailability.writer.RoomAvailabilityChunkWriter;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.domain.roomavailability.RoomAvailability;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class OpenRoomAvailabilityJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Bean
	public Job originOpenAvailabilityChunkJob(Step originOpenAvailabilityChunkStep) {
		String jobName = "originOpenAvailabilityChunkJob";

		return new JobBuilder(jobName, jobRepository)
			.start(originOpenAvailabilityChunkStep)
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step originOpenAvailabilityChunkStep(
		RoomAutoPolicyChunkReader reader,
		OriginOpenAvailabilityChunkProcessor originProcessor,
		RoomAvailabilityChunkWriter writer
	) {
		String stepName = "originOpenAvailabilityChunkStep";

		return new StepBuilder(stepName, jobRepository)
			.<List<RoomAutoAvailabilityPolicy>, List<RoomAvailability>>chunk(1, transactionManager)
			.reader(reader)
			.processor(originProcessor)
			.writer(writer)
			.build();
	}

	@Bean
	public Job improvedOpenAvailabilityChunkJob(Step improvedOpenAvailabilityChunkStep) {
		String jobName = "improvedOpenAvailabilityChunkJob";

		return new JobBuilder(jobName, jobRepository)
			.start(improvedOpenAvailabilityChunkStep)
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step improvedOpenAvailabilityChunkStep(
		RoomAutoPolicyChunkReader reader,
		ImprovedOpenAvailabilityChunkProcessor improvedProcessor,
		RoomAvailabilityChunkWriter writer
	) {
		String stepName = "improvedOpenAvailabilityChunkStep";

		return new StepBuilder(stepName, jobRepository)
			.<List<RoomAutoAvailabilityPolicy>, List<RoomAvailability>>chunk(1, transactionManager)
			.reader(reader)
			.processor(improvedProcessor)
			.writer(writer)
			.build();
	}

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
}

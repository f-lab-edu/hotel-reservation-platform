package com.reservation.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class AutoOpenAvailabilityScheduler {

	private final JobLauncher jobLauncher;
	private final Job autoOpenAvailabilityJob;

	@Scheduled(cron = "0 0 0 * * *") // 임의로 매일 자정에 실행되도록 세팅
	public void runBatch() {
		try {
			jobLauncher.run(autoOpenAvailabilityJob, new JobParametersBuilder()
				.addLong("run.id", System.currentTimeMillis())
				.toJobParameters()
			);
		} catch (Exception e) {
			log.error("Batch failed", e);
		}
	}
}

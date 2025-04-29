package com.reservation.batch.batchadmin.service.dto;

import java.time.LocalDateTime;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;

public record JobExecutionSummary(
	Long executionId,
	String jobName,
	BatchStatus status,
	LocalDateTime startTime,
	LocalDateTime endTime) {

	public static JobExecutionSummary from(JobExecution jobExecution) {
		return new JobExecutionSummary(
			jobExecution.getId(),
			jobExecution.getJobInstance().getJobName(),
			jobExecution.getStatus(),
			jobExecution.getStartTime(),
			jobExecution.getEndTime()
		);
	}
}

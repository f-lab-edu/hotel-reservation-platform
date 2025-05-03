package com.reservation.batch.batchadmin.service.dto;

import java.time.LocalDateTime;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParameters;

public record TriggerJobResult(
	Long executionId,
	String jobName,
	BatchStatus status,
	LocalDateTime startTime,
	JobParameters parameters
) {
}

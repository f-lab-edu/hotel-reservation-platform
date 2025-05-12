package com.reservation.batch.batchadmin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.batch.batchadmin.controller.request.TriggerJobRequest;
import com.reservation.batch.batchadmin.service.BatchAdminService;
import com.reservation.batch.batchadmin.service.dto.JobExecutionSummary;
import com.reservation.batch.batchadmin.service.dto.TriggerJobResult;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class BatchAdminController {
	private final BatchAdminService batchAdminService;

	@GetMapping("/jobs")
	@Operation(summary = "모든 Job 이름 확인 API", description = "모든 Job 이름을 확인합니다.")
	public ApiResponse<List<String>> getAllJobNames() {
		List<String> jobNames = batchAdminService.getAllJobNames();

		return ApiResponse.ok(jobNames);
	}

	@GetMapping("/executions")
	@Operation(summary = "최근 실행된 Job 확인 API", description = "최근 실행된 Job 실행 결과를 확인합니다.")
	public ApiResponse<List<JobExecutionSummary>> getRecentJobExecutions() {
		List<JobExecutionSummary> jobExecutionSummaries = batchAdminService.getRecentJobExecutions();

		return ApiResponse.ok(jobExecutionSummaries);
	}

	@PostMapping("/trigger")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Job 실행 API", description = "특정 Job 실행합니다.")
	public ApiResponse<TriggerJobResult> triggerJob(@Valid @RequestBody TriggerJobRequest request) {
		Map<String, String> jobParameters =
			request.jobParametersOrNull() == null ? Map.of() : request.jobParametersOrNull();

		TriggerJobResult triggerJobResult = batchAdminService.triggerJob(request.jobName(), jobParameters);

		return ApiResponse.ok(triggerJobResult);
	}

	@PostMapping("/retry/{executionId}")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Job 재실행 API", description = "실패한 JobExecution 재시도합니다.")
	public ApiResponse<TriggerJobResult> retryJob(@PathVariable long executionId) {
		TriggerJobResult result = batchAdminService.retryJob(executionId);

		return ApiResponse.ok(result);
	}

	@PostMapping("/stop/{executionId}")
	@Operation(summary = "Job 중단 API", description = "진행 중인 JobExecution 중단 요청합니다.")
	public ApiResponse<Void> stopJob(@PathVariable long executionId) {
		batchAdminService.stopJob(executionId);

		return ApiResponse.noContent();
	}
}

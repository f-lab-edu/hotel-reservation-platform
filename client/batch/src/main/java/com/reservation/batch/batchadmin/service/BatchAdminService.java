package com.reservation.batch.batchadmin.service;

import java.util.List;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;

import com.reservation.batch.batchadmin.service.dto.JobExecutionSummary;
import com.reservation.batch.batchadmin.service.dto.TriggerJobResult;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchAdminService {
	private static final String DEFAULT_RUN_ID = "run.id";

	private final JobRegistry jobRegistry;
	private final JobExplorer jobExplorer;
	private final JobRepository jobRepository;
	private final TaskExecutorJobLauncher taskExecutorJobLauncher;

	public List<String> getAllJobNames() {
		return jobRegistry.getJobNames()
			.stream()
			.toList();
	}

	public List<JobExecutionSummary> getRecentJobExecutions() {
		return jobExplorer.findRunningJobExecutions(null)
			.stream().map(JobExecutionSummary::from).toList();
	}

	public TriggerJobResult triggerJob(String jobName, Map<String, String> jobParameterMap) {
		Job findJob = getJob(jobName);

		// JobParameters 변환
		JobParametersBuilder builder = new JobParametersBuilder();
		jobParameterMap.forEach(builder::addString);

		// run.id는 매번 달라야 JobInstance 중복되지 않음
		builder.addLong(DEFAULT_RUN_ID, System.currentTimeMillis());

		JobParameters jobParameters = builder.toJobParameters();
		JobExecution execution;
		try {
			execution = taskExecutorJobLauncher.run(findJob, jobParameters);
		} catch (Exception e) {
			log.error("Execution Trigger Job Error", e);
			throw ErrorCode.CONFLICT.exception("Job 실행 실패: " + e.getMessage());
		}

		return new TriggerJobResult(
			execution.getId(),
			jobName,
			execution.getStatus(),
			execution.getStartTime(),
			execution.getJobParameters()
		);
	}

	private Job getJob(String jobName) {
		try {
			return jobRegistry.getJob(jobName);
		} catch (NoSuchJobException e) {
			log.error("Find Trigger Job Error", e);
			throw ErrorCode.CONFLICT.exception("존재하지 않는 Job 입니다.");
		}
	}

	public TriggerJobResult retryJob(long executionId) {
		JobExecution previousExecution = jobExplorer.getJobExecution(executionId);
		if (previousExecution == null) {
			throw ErrorCode.NOT_FOUND.exception("해당 ExecutionId를 찾을 수 없습니다.");
		}
		if (!previousExecution.getStatus().isUnsuccessful()) {
			throw ErrorCode.CONFLICT.exception("실패한 Job만 재시도할 수 있습니다.");
		}

		JobInstance jobInstance = previousExecution.getJobInstance();

		Job retryJob = getJob(jobInstance.getJobName());
		JobParameters jobParameters = previousExecution.getJobParameters();

		JobExecution newExecution;
		try {
			newExecution = taskExecutorJobLauncher.run(retryJob, jobParameters);
		} catch (Exception e) {
			log.error("retryJob Error", e);
			throw ErrorCode.CONFLICT.exception("Job 재시도 실패: " + e.getMessage());
		}

		return new TriggerJobResult(
			newExecution.getId(),
			retryJob.getName(),
			newExecution.getStatus(),
			newExecution.getStartTime(),
			newExecution.getJobParameters()
		);
	}

	private JobExecution getJobExecution(long executionId) {
		JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
		if (jobExecution == null) {
			throw ErrorCode.NOT_FOUND.exception("해당 ExecutionId를 찾을 수 없습니다.");
		}
		
		return jobExecution;
	}

	public void stopJob(long executionId) {
		JobExecution jobExecution = getJobExecution(executionId);
		if (!jobExecution.isRunning()) {
			throw ErrorCode.CONFLICT.exception("현재 실행 중인 Job만 중단할 수 있습니다.");
		}

		jobExecution.setStatus(BatchStatus.STOPPING); // 상태를 STOPPING으로 변경
		jobExecution.setExitStatus(new ExitStatus("STOPPING_BY_ADMIN"));
		jobRepository.update(jobExecution);
	}
}

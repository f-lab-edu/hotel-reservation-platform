package com.reservation.batch.job.roomabailabilitysummary.reader;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.reservation.batch.repository.RoomAutoAvailabilityPolicyRepository;
import com.reservation.batch.repository.dto.CursorPage;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class RoomAutoAvailabilityPolicyReader {
	private static final int READ_SIZE = 1000;

	private final RoomAutoAvailabilityPolicyRepository autoPolicyRepository;

	@Value("#{stepExecution.jobExecution}")
	private JobExecution jobExecution;

	public CursorPage<RoomAutoAvailabilityPolicy, Long> read(Long lastSeenId) {
		if (jobExecution.isStopping()) {
			log.error("Job execution stopped {}", jobExecution.getExitStatus().getExitDescription());
			throw ErrorCode.CONFLICT.exception("Job 중단 요청됨 → Reader 중단");
		}

		return autoPolicyRepository.fetchNextPage(lastSeenId, READ_SIZE);
	}
}

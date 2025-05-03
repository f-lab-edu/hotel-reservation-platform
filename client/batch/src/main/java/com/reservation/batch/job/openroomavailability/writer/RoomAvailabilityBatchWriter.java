package com.reservation.batch.job.openroomavailability.writer;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.reservation.domain.roomavailability.RoomAvailability;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class RoomAvailabilityBatchWriter {
	private final JdbcTemplate jdbcTemplate;
	
	@Value("#{stepExecution.jobExecution}")
	private JobExecution jobExecution;

	public void write(List<RoomAvailability> writeRoomAvailabilities) {
		if (jobExecution.isStopping()) {
			log.error("Job execution stopped {}", jobExecution.getExitStatus().getExitDescription());
			throw ErrorCode.CONFLICT.exception("Job 중단 요청됨 → Writer 중단");
		}

		if (writeRoomAvailabilities.isEmpty()) {
			return;
		}

		Map<YearMonth, List<RoomAvailability>> partitionedMap = writeRoomAvailabilities.stream()
			.collect(Collectors.groupingBy(ra -> YearMonth.from(ra.getDate())));

		partitionedMap.entrySet().parallelStream().forEach(entry -> {
			List<RoomAvailability> subList = entry.getValue();
			jdbcTemplate.batchUpdate(
				"INSERT INTO room_availability (created_at, updated_at, available_count, date, room_id, price) VALUES (?, ?, ?, ?, ?, ?)",
				subList,
				subList.size(),
				(ps, roomAvailability) -> {
					ps.setDate(1, Date.valueOf(LocalDate.now()));
					ps.setDate(2, Date.valueOf(LocalDate.now()));
					ps.setInt(3, roomAvailability.getAvailableCount());
					ps.setDate(4, Date.valueOf(roomAvailability.getDate()));
					ps.setLong(5, roomAvailability.getRoomTypeId());
					ps.setInt(6, roomAvailability.getPrice());
				}
			);
		});
	}
}

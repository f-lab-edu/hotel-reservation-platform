package com.reservation.batch.job.openroomavailability.writer;

import java.sql.Date;
import java.util.List;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
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
public class RoomAvailabilityBatchWriter implements ItemWriter<List<RoomAvailability>> {
	private final static int BULK_INSERT_SIZE = 10000;

	private final JdbcTemplate jdbcTemplate;
	@Value("#{stepExecution.jobExecution}")
	private JobExecution jobExecution;

	@Override
	public void write(Chunk<? extends List<RoomAvailability>> chunk) {
		if (jobExecution.isStopping()) {
			log.error("Job execution stopped {}", jobExecution.getExitStatus().getExitDescription());
			throw ErrorCode.CONFLICT.exception("Job 중단 요청됨 → Writer 중단");
		}

		List<RoomAvailability> flatList = chunk.getItems().stream()
			.flatMap(List::stream)
			.toList();

		if (flatList.isEmpty()) {
			return;
		}

		jdbcTemplate.batchUpdate(
			"INSERT INTO room_availability (room_id, date, available_count, price) VALUES (?, ?, ?, ?)",
			flatList,
			BULK_INSERT_SIZE, // Batch Size (한 번에 몇개씩 insert할지)
			(ps, roomAvailability) -> {
				ps.setLong(1, roomAvailability.getRoomId());
				ps.setDate(2, Date.valueOf(roomAvailability.getDate()));
				ps.setInt(3, roomAvailability.getAvailableCount());
				ps.setInt(4, roomAvailability.getPrice());
			}
		);
	}
}

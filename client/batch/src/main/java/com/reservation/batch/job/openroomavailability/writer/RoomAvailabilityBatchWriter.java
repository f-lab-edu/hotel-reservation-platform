package com.reservation.batch.job.openroomavailability.writer;

import java.sql.Date;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.reservation.domain.roomavailability.RoomAvailability;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class RoomAvailabilityBatchWriter implements ItemWriter<List<RoomAvailability>> {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void write(Chunk<? extends List<RoomAvailability>> chunk) {
		List<RoomAvailability> flatList = chunk.getItems().stream()
			.flatMap(List::stream)
			.toList();

		if (flatList.isEmpty()) {
			return;
		}

		jdbcTemplate.batchUpdate(
			"INSERT INTO room_availability (room_id, date, available_count, price) VALUES (?, ?, ?, ?)",
			flatList,
			10000, // Batch Size (한 번에 몇개씩 insert할지)
			(ps, roomAvailability) -> {
				ps.setLong(1, roomAvailability.getRoomId());
				ps.setDate(2, Date.valueOf(roomAvailability.getDate()));
				ps.setInt(3, roomAvailability.getAvailableCount());
				ps.setInt(4, roomAvailability.getPrice());
			}
		);
	}
}

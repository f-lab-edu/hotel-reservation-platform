package com.reservation.batch.job.openroomavailability.writer;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.reservation.batch.utils.Perf;
import com.reservation.domain.roomavailability.RoomAvailability;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class RoomAvailabilityChunkWriter implements ItemWriter<List<RoomAvailability>> {
	private final JdbcTemplate jdbcTemplate;

	@Override
	public void write(Chunk<? extends List<RoomAvailability>> output) {
		List<RoomAvailability> writeAvailabilities = output.getItems().stream()
			.flatMap(List::stream)
			.toList();

		if (writeAvailabilities.isEmpty()) {
			return;
		}

		Perf perf = new Perf();

		String insertSql =
			"INSERT INTO room_availability (created_at, updated_at, available_count, date, room_type_id, price)"
				+ " VALUES (?, ?, ?, ?, ?, ?)";

		jdbcTemplate.batchUpdate(
			insertSql,
			writeAvailabilities,
			writeAvailabilities.size(),
			(ps, availability) -> {
				ps.setDate(1, Date.valueOf(LocalDate.now()));
				ps.setDate(2, Date.valueOf(LocalDate.now()));
				ps.setInt(3, availability.getAvailableCount());
				ps.setDate(4, Date.valueOf(availability.getDate()));
				ps.setLong(5, availability.getRoomTypeId());
				ps.setInt(6, availability.getPrice());
			}
		);
		perf.log("Write rows", writeAvailabilities.size());
	}
}

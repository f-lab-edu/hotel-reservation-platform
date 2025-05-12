package com.reservation.batch.job.openroomavailability.writer;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.reservation.domain.roomavailability.RoomAvailability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomAvailabilityTaskletWriter {
	private final JdbcTemplate jdbcTemplate;

	public void write(List<RoomAvailability> outputAvailabilities) {
		if (outputAvailabilities.isEmpty()) {
			return;
		}

		String insertSql =
			"INSERT INTO room_availability (id, created_at, updated_at, available_count, date, room_type_id, price)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?)";

		jdbcTemplate.batchUpdate(
			insertSql,
			outputAvailabilities,
			outputAvailabilities.size(),
			(ps, availability) -> {
				ps.setLong(1, availability.getId());
				ps.setDate(2, Date.valueOf(LocalDate.now()));
				ps.setDate(3, Date.valueOf(LocalDate.now()));
				ps.setInt(4, availability.getAvailableCount());
				ps.setDate(5, Date.valueOf(availability.getDate()));
				ps.setLong(6, availability.getRoomTypeId());
				ps.setInt(7, availability.getPrice());
			}
		);
	}
}

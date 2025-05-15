package com.reservation.batch.job.openroomavailability.taskletStep.writer;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.reservation.domain.roomavailability.OriginRoomAvailability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomAvailabilityTaskletWriter {
	private final JdbcTemplate jdbcTemplate;

	public void write(List<OriginRoomAvailability> outputAvailabilities) {
		if (outputAvailabilities.isEmpty()) {
			return;
		}

		String insertSql =
			"INSERT INTO origin_room_availability (created_at, updated_at, available_count, date, room_type_id, price)"
				+ " VALUES (?, ?, ?, ?, ?, ?)";

		jdbcTemplate.batchUpdate(
			insertSql,
			outputAvailabilities,
			outputAvailabilities.size(),
			(ps, availability) -> {
				ps.setDate(1, Date.valueOf(LocalDate.now()));
				ps.setDate(2, Date.valueOf(LocalDate.now()));
				ps.setInt(3, availability.getAvailableCount());
				ps.setDate(4, Date.valueOf(availability.getOpenDate()));
				ps.setLong(5, availability.getRoomTypeId());
				ps.setInt(6, availability.getPrice());
			}
		);
	}
}

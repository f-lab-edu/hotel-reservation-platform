package com.reservation.domain.roomavailability;

import java.time.LocalDate;

import com.reservation.domain.base.BaseEntity;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
CREATE TABLE `csv_room_availability` (
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `available_count` int NOT NULL,
  `date` date NOT NULL,
  `price` int NOT NULL,
  `room_type_id` bigint NOT NULL,
  PRIMARY KEY `room_type_pk` (`room_type_id`, `date`),
  KEY `room_type_id` (`room_type_id`),
  CONSTRAINT `csv_room_availability_fk` FOREIGN KEY (`room_type_id`) REFERENCES `room_type` (`id`)
);
* */

@Getter
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CsvRoomAvailability extends BaseEntity {
	@Column(nullable = false)
	private long roomTypeId; // 룸 타입 ID

	@Column(nullable = false)
	private LocalDate openDate; // 예약 가능 날짜

	@Column(nullable = false)
	private Integer price; // 가격

	@Column(nullable = false)
	private Integer availableCount; // 예약 가능 개수

	@Builder
	public CsvRoomAvailability(Long id, long roomTypeId, LocalDate openDate, int price, int availableCount) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("숙소 ID는 0보다 커야 합니다.");
		}
		if (roomTypeId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 ID는 0보다 커야 합니다.");
		}
		if (openDate == null) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 날짜는 필수입니다.");
		}
		if (price < 1000) {
			throw ErrorCode.BAD_REQUEST.exception("가격은 1000 이상이어야 합니다.");
		}
		if (availableCount < 0) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 개수는 0 이상이어야 합니다.");
		}

		this.id = id;
		this.roomTypeId = roomTypeId;
		this.openDate = openDate;
		this.price = price;
		this.availableCount = availableCount;
	}
}

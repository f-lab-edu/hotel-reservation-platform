package com.reservation.domain.roomavailability;

import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 CREATE TABLE hotel.`room_availability` (
  `room_type_id` bigint NOT NULL,
  `date` date NOT NULL,
  `available_count` int NOT NULL,
  `price` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY `room_type_pk` (`room_type_id`, `date`)
) PARTITION BY RANGE COLUMNS(date) (
    PARTITION p202505 VALUES LESS THAN ('2025-06-01'),
    PARTITION p202506 VALUES LESS THAN ('2025-07-01'),
    PARTITION p202507 VALUES LESS THAN ('2025-08-01'),
    PARTITION p202508 VALUES LESS THAN ('2025-09-01'),
    PARTITION p202509 VALUES LESS THAN ('2025-10-01'),
    PARTITION p202510 VALUES LESS THAN ('2025-11-01'),
    PARTITION p202511 VALUES LESS THAN ('2025-12-01'),
    PARTITION pMAX VALUES LESS THAN (MAXVALUE)
);
 * */
@Getter
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PartitionRoomAvailability {
	@Id
	@EmbeddedId
	private RoomAvailabilityId id; // 룸 타입 ID + 예약 가능 날짜

	@Column(nullable = false)
	private Integer price; // 가격

	@Column(nullable = false)
	private Integer availableCount; // 예약 가능 개수

	@Builder
	public PartitionRoomAvailability(RoomAvailabilityId id, int price, int availableCount) {
		if (id == null) {
			throw ErrorCode.CONFLICT.exception("룸 예약 가용 ID는 필수입니다.");
		}
		if (price < 1000) {
			throw ErrorCode.BAD_REQUEST.exception("가격은 1000 이상이어야 합니다.");
		}
		if (availableCount < 0) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 개수는 0 이상이어야 합니다.");
		}
		
		this.id = id;
		this.price = price;
		this.availableCount = availableCount;
	}
}

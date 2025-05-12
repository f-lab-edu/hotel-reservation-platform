package com.reservation.domain.roomtype;

import com.reservation.domain.base.BaseEntity;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RoomType extends BaseEntity {
	@Column(nullable = false)
	private Long accommodationId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer capacity;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = true, name = "description")
	private String descriptionOrNull;

	@Column(nullable = false)
	private Integer roomCount; // 이 방 타입에 몇 개의 방이 존재하는지

	@Builder
	public RoomType(
		Long id,
		Long accommodationId,
		String name,
		Integer capacity,
		Integer price,
		String descriptionOrNull,
		Integer roomCount
	) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("룸 타입 ID는 0보다 커야 합니다.");
		}
		if (accommodationId == null || accommodationId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("숙소 정보는 필수입니다.");
		}
		if (name == null || name.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("룸 타입 이름은 필수입니다.");
		}
		if (capacity == null || capacity <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 타입 수용 인원은 0보다 커야 합니다.");
		}
		if (price == null || price < 1000) {
			throw ErrorCode.BAD_REQUEST.exception("룸 타입 가격은 1,000보다 커야 합니다.");
		}
		if (roomCount == null || roomCount <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 타입 방 개수는 0보다 커야 합니다.");
		}

		this.id = id;
		this.accommodationId = accommodationId;
		this.name = name;
		this.capacity = capacity;
		this.price = price;
		this.descriptionOrNull = descriptionOrNull;
		this.roomCount = roomCount;
	}
}

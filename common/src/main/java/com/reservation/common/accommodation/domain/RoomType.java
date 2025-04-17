package com.reservation.common.accommodation.domain;

import com.reservation.common.domain.BaseEntity;
import com.reservation.commonmodel.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;

@Getter
@Entity
public class RoomType extends BaseEntity {
	@Column(nullable = false)
	private Long accommodationId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer capacity;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = true)
	private String descriptionOrNull;

	@Column(nullable = false)
	private Integer roomCount; // 이 방 타입에 몇 개의 방이 존재하는지

	protected RoomType() {
	}

	public RoomType(Long id, Long accommodationId, String name, Integer capacity, Integer price,
		String descriptionOrNull, Integer roomCount) {
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

	public static class RoomTypeBuilder {
		private Long id;
		private Long accommodationId;
		private String name;
		private Integer capacity;
		private Integer price;
		private String descriptionOrNull;
		private Integer roomCount;

		public RoomTypeBuilder id(Long id) {
			this.id = id;
			return this;
		}

		public RoomTypeBuilder accommodationId(Long accommodationId) {
			this.accommodationId = accommodationId;
			return this;
		}

		public RoomTypeBuilder name(String name) {
			this.name = name;
			return this;
		}

		public RoomTypeBuilder capacity(Integer capacity) {
			this.capacity = capacity;
			return this;
		}

		public RoomTypeBuilder price(Integer price) {
			this.price = price;
			return this;
		}

		public RoomTypeBuilder descriptionOrNull(String descriptionOrNull) {
			this.descriptionOrNull = descriptionOrNull;
			return this;
		}

		public RoomTypeBuilder roomCount(Integer roomCount) {
			this.roomCount = roomCount;
			return this;
		}

		public RoomType build() {
			return new RoomType(id, accommodationId, name, capacity, price, descriptionOrNull, roomCount);
		}
	}
}

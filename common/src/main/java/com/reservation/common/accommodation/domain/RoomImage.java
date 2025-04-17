package com.reservation.common.accommodation.domain;

import com.reservation.common.domain.BaseEntity;
import com.reservation.commonmodel.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;

@Getter
@Entity
public class RoomImage extends BaseEntity {
	@Column(nullable = false)
	private Long roomTypeId;

	@Column(nullable = false)
	private String imageUrl;

	@Column(nullable = false)
	private Integer displayOrder; // 이미지 노출 순서

	@Column(nullable = false)
	private Boolean isMainImage;

	protected RoomImage() {
	}

	public RoomImage(Long id, Long roomTypeId, String imageUrl, Integer displayOrder, Boolean isMainImage) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("룸 이미지 ID는 0보다 커야 합니다.");
		}
		if (roomTypeId == null || roomTypeId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 타입 정보는 필수입니다.");
		}
		if (imageUrl == null || imageUrl.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("룸 이미지 URL은 필수입니다.");
		}
		if (displayOrder == null || displayOrder < 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 이미지 노출 순서는 0 이상이어야 합니다.");
		}
		if (isMainImage == null) {
			throw ErrorCode.BAD_REQUEST.exception("룸 이미지 메인 여부는 필수입니다.");
		}
		this.id = id;
		this.roomTypeId = roomTypeId;
		this.imageUrl = imageUrl;
		this.displayOrder = displayOrder;
		this.isMainImage = isMainImage;
	}

	public static class RoomImageBuilder {
		private Long id;
		private Long roomTypeId;
		private String imageUrl;
		private Integer displayOrder;
		private Boolean isMainImage;

		public RoomImageBuilder id(Long id) {
			this.id = id;
			return this;
		}

		public RoomImageBuilder roomTypeId(Long roomTypeId) {
			this.roomTypeId = roomTypeId;
			return this;
		}

		public RoomImageBuilder imageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
			return this;
		}

		public RoomImageBuilder displayOrder(Integer displayOrder) {
			this.displayOrder = displayOrder;
			return this;
		}

		public RoomImageBuilder isMainImage(Boolean isMainImage) {
			this.isMainImage = isMainImage;
			return this;
		}

		public RoomImage build() {
			return new RoomImage(id, roomTypeId, imageUrl, displayOrder, isMainImage);
		}
	}
}

package com.reservation.domain.roomimage;

import com.reservation.domain.base.BaseEntity;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Builder;
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

	@Builder
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

	public void updateDisplayOrderAndIsMainImage(int displayOrder, boolean isMainImage) {
		if (displayOrder < 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 이미지 노출 순서는 0 이상이어야 합니다.");
		}
		this.displayOrder = displayOrder;
		this.isMainImage = isMainImage;
	}
}

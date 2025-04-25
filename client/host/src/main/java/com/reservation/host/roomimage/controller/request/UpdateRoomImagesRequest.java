package com.reservation.host.roomimage.controller.request;

import java.util.List;

import com.reservation.host.roomimage.service.dto.DefaultRoomImageInfo;
import com.reservation.support.exception.ErrorCode;

public record UpdateRoomImagesRequest(
	List<DefaultRoomImageInfo> roomImages
) {
	public List<DefaultRoomImageInfo> validateRoomImages() {
		if (roomImages == null) {
			throw ErrorCode.BAD_REQUEST.exception("방 이미지 정보가 없습니다.");
		}
		
		int mainImageCount = 0;
		for (DefaultRoomImageInfo defaultRoomImageInfo : roomImages) {
			if (defaultRoomImageInfo.id() != null && defaultRoomImageInfo.id() <= 0) {
				throw ErrorCode.BAD_REQUEST.exception("방 이미지 ID가 잘못되었습니다.");
			}
			if (defaultRoomImageInfo.uploadUrl() == null || defaultRoomImageInfo.uploadUrl().isBlank()) {
				throw ErrorCode.BAD_REQUEST.exception("방 이미지 URL이 없습니다.");
			}
			if (defaultRoomImageInfo.displayOrder() <= 0) {
				throw ErrorCode.BAD_REQUEST.exception("방 이미지 순서가 잘못되었습니다.");
			}
			if (defaultRoomImageInfo.isMainImage()) {
				mainImageCount++;
				if (mainImageCount > 1) {
					throw ErrorCode.BAD_REQUEST.exception("방 이미지는 하나만 메인 이미지로 설정할 수 있습니다.");
				}
			}
		}

		return roomImages;
	}
}

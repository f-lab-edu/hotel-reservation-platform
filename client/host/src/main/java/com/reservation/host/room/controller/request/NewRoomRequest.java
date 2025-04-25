package com.reservation.host.room.controller.request;

import com.reservation.host.room.service.dto.DefaultRoomInfo;
import com.reservation.support.exception.ErrorCode;

public record NewRoomRequest(
	long accommodationId,
	String name,
	int capacity,
	int price,
	String descriptionOrNull,
	int roomCount
) {
	public DefaultRoomInfo validateToDefaultRoomInfo() {
		if (accommodationId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("숙소 ID는 0보다 커야 합니다.");
		}
		if (name == null || name.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("방 이름은 비어있을 수 없습니다.");
		}
		if (capacity <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("수용 인원은 0보다 커야 합니다.");
		}
		if (price < 1000) {
			throw ErrorCode.BAD_REQUEST.exception("가격은 1000원 이상이어야 합니다.");
		}
		if (roomCount < 0) {
			throw ErrorCode.BAD_REQUEST.exception("방 개수는 0개 이상이어야 합니다.");
		}

		return new DefaultRoomInfo(
			accommodationId,
			name,
			capacity,
			price,
			descriptionOrNull,
			roomCount
		);
	}
}

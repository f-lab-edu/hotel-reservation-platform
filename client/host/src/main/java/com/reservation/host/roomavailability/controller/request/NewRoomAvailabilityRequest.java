package com.reservation.host.roomavailability.controller.request;

import java.time.LocalDate;

import com.reservation.host.roomavailability.service.dto.DefaultRoomAvailabilityInfo;
import com.reservation.support.exception.ErrorCode;

public record NewRoomAvailabilityRequest(
	LocalDate date,
	int price,
	int availableCount
) {
	public DefaultRoomAvailabilityInfo validateToDefaultRoomAvailabilityInfo(long roomTypeId) {
		if (roomTypeId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 ID는 0보다 커야 합니다.");
		}
		if (date == null || date.isBefore(LocalDate.now())) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 날짜는 오늘 이후여야 합니다.");
		}
		if (price < 1000) {
			throw ErrorCode.BAD_REQUEST.exception("가격은 1000 이상이어야 합니다.");
		}
		if (availableCount < 0) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 개수는 0 이상이어야 합니다.");
		}

		return new DefaultRoomAvailabilityInfo(roomTypeId, date, price, availableCount);
	}
}

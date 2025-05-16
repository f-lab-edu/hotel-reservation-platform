package com.reservation.customer.roomavailability.controller.request;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.reservation.customer.roomavailability.service.dto.DefaultRoomAvailabilitySearchInfo;
import com.reservation.support.exception.ErrorCode;

public record FindAvailableRoomTypesRequest(
	LocalDate checkIn,
	LocalDate checkOut,
	Integer capacity
) {
	public DefaultRoomAvailabilitySearchInfo validateToSearchInfo() {
		if (checkIn == null || checkOut == null || capacity == null) {
			throw ErrorCode.VALIDATION_ERROR.exception("체크인, 체크아웃, 인원 수는 필수 입력값입니다.");
		}
		if (checkIn.isBefore(LocalDate.now())) {
			throw ErrorCode.VALIDATION_ERROR.exception("체크인 날짜는 오늘 이후여야 합니다.");
		}
		if (checkIn.isAfter(checkOut)) {
			throw ErrorCode.VALIDATION_ERROR.exception("체크인 날짜는 체크아웃 날짜보다 이전이어야 합니다.");
		}
		if (ChronoUnit.DAYS.between(checkIn, checkOut) > 31) {
			throw ErrorCode.VALIDATION_ERROR.exception("최대 예약 가능일은 31일입니다.");
		}
		if (capacity <= 0) {
			throw ErrorCode.VALIDATION_ERROR.exception("인원 수는 1명 이상이어야 합니다.");
		}
		return new DefaultRoomAvailabilitySearchInfo(checkIn, checkOut, capacity);
	}
}

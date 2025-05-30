package com.reservation.customer.reservation.controller.request;

import java.time.LocalDate;

import com.reservation.customer.reservation.service.dto.CreateReservationCommand;
import com.reservation.support.exception.ErrorCode;

public record RoomReservationRequest(
	Long roomTypeId,
	LocalDate checkIn,
	LocalDate checkOut,
	Integer guestCount
) {
	public CreateReservationCommand validateToCreateCommand() {
		if (roomTypeId == null || roomTypeId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("roomTypeId는 필수입니다.");
		}
		if (checkIn == null || checkOut == null) {
			throw ErrorCode.BAD_REQUEST.exception("체크인/체크아웃 날짜는 필수입니다.");
		}
		if (!checkOut.isAfter(checkIn)) {
			throw ErrorCode.BAD_REQUEST.exception("체크아웃은 체크인보다 이후여야 합니다.");
		}
		if (checkIn.isBefore(LocalDate.now())) {
			throw ErrorCode.BAD_REQUEST.exception("체크인은 오늘 이후여야 합니다.");
		}
		if (guestCount == null || guestCount <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("투숙 인원은 1명 이상이어야 합니다.");
		}

		return new CreateReservationCommand(
			roomTypeId,
			checkIn,
			checkOut,
			guestCount
		);
	}
}

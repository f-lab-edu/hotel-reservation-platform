package com.reservation.customer.reservation.controller.request;

import java.time.LocalDate;

import com.reservation.customer.reservation.service.dto.CreateReservationCommand;
import com.reservation.support.exception.ErrorCode;

public record RoomReservationRequest(
	Long roomTypeId,
	LocalDate checkIn,
	LocalDate checkOut,
	Integer guestCount,
	String customerName,
	String phoneNumber,
	String paymentMethod,
	boolean agreeToTerms
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
		if (customerName == null || customerName.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("예약자 이름은 필수입니다.");
		}
		if (phoneNumber == null || phoneNumber.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("연락처는 필수입니다.");
		}
		if (paymentMethod == null || paymentMethod.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("결제 수단은 필수입니다.");
		}
		if (!agreeToTerms) {
			throw ErrorCode.BAD_REQUEST.exception("약관에 동의해야 예약이 가능합니다.");
		}

		return new CreateReservationCommand(
			roomTypeId,
			checkIn,
			checkOut,
			guestCount,
			customerName,
			phoneNumber,
			paymentMethod
		);
	}
}

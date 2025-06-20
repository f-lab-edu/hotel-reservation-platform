package com.reservation.customer.payment.controller.request;

import java.time.LocalDate;

import com.reservation.customer.payment.service.dto.PaymentCheckCommand;
import com.reservation.support.exception.ErrorCode;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PaymentCheckRequest(
	@NotNull @NotEmpty
	String paymentUid,

	@NotNull
	Long roomTypeId,

	@NotNull
	LocalDate checkIn,

	@NotNull
	LocalDate checkOut,

	@NotNull
	String phoneNumber,

	@NotNull
	Integer guestsCount
) {
	public PaymentCheckCommand validateAndToCommand(long memberId) {
		if (paymentUid == null || paymentUid.isEmpty()) {
			throw ErrorCode.VALIDATION_ERROR.exception("Payment UID must not be empty");
		}
		if (roomTypeId == null || roomTypeId <= 0) {
			throw ErrorCode.VALIDATION_ERROR.exception("Room Type ID must be a positive number");
		}
		if (checkIn == null || checkOut == null || checkIn.isAfter(checkOut)) {
			throw ErrorCode.VALIDATION_ERROR.exception("Check-in and Check-out dates must be valid");
		}
		if (phoneNumber == null || phoneNumber.isEmpty()) {
			throw ErrorCode.VALIDATION_ERROR.exception("Phone number must not be empty");
		}
		if (guestsCount == null || guestsCount <= 0) {
			throw ErrorCode.VALIDATION_ERROR.exception("Guests count must be a positive number");
		}
		
		return new PaymentCheckCommand(
			memberId,
			paymentUid,
			roomTypeId,
			checkIn,
			checkOut,
			phoneNumber,
			guestsCount
		);
	}
}

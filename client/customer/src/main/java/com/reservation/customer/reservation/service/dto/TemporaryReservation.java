package com.reservation.customer.reservation.service.dto;

import java.time.LocalDate;

import com.reservation.domain.reservation.Reservation;
import com.reservation.domain.reservation.enums.ReservationStatus;

public record TemporaryReservation(
	Long memberId,
	Long roomTypeId,
	LocalDate checkIn,
	LocalDate checkOut,
	int guestCount,
	String phoneNumber,
	int totalPrice
) {
	public String redisKey() {
		return String.format("reservation:%d:%d:%s:%s",
			memberId, roomTypeId, checkIn, checkOut
		);
	}

	public Reservation toReservation() {
		return Reservation.builder()
			.roomTypeId(roomTypeId)
			.memberId(memberId)
			.checkIn(checkIn)
			.checkOut(checkOut)
			.guestCount(guestCount)
			.phoneNumber(phoneNumber)
			.totalPrice(totalPrice)
			.status(ReservationStatus.PAID) // 결제 검증 성공
			.build();
	}
}

package com.reservation.domain.reservation;

import java.time.LocalDate;

import com.reservation.domain.base.BaseEntity;
import com.reservation.domain.reservation.enums.ReservationStatus;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {
	@Column(nullable = false)
	private Long roomTypeId;

	@Column(nullable = false)
	private Long memberId;

	@Column(nullable = false)
	private LocalDate checkIn;

	@Column(nullable = false)
	private LocalDate checkOut;

	@Column(nullable = false)
	private Integer guestCount;

	@Column(nullable = false)
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReservationStatus status;

	@Column(nullable = false)
	private Integer totalPrice;

	@Builder
	public Reservation(
		Long id,
		Long roomTypeId,
		Long memberId,
		LocalDate checkIn,
		LocalDate checkOut,
		Integer guestCount,
		String phoneNumber,
		Integer totalPrice,
		ReservationStatus status
	) {
		if (roomTypeId == null || roomTypeId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("roomTypeId는 필수입니다.");
		}

		if (memberId == null || memberId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("memberId 필수입니다.");
		}

		if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
			throw ErrorCode.BAD_REQUEST.exception("체크인/체크아웃 날짜가 유효하지 않습니다.");
		}

		if (guestCount == null || guestCount <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("투숙 인원은 필수입니다.");
		}

		if (phoneNumber == null || phoneNumber.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("연락처는 필수입니다.");
		}

		if (totalPrice == null || totalPrice <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("총 금액은 필수입니다.");
		}

		this.id = id;
		this.roomTypeId = roomTypeId;
		this.memberId = memberId;
		this.checkIn = checkIn;
		this.checkOut = checkOut;
		this.guestCount = guestCount;
		this.phoneNumber = phoneNumber;
		this.totalPrice = totalPrice;
		this.status = status != null ? status : ReservationStatus.CANCELED;
	}

	public void markConfirmed() {
		this.status = ReservationStatus.CONFIRMED;
	}

	public void markPaid() {
		this.status = ReservationStatus.PAID;
	}

	public void markPaidError() {
		this.status = ReservationStatus.PAID_ERROR;
	}

	public void markCanceled() {
		if (this.status != ReservationStatus.PAID) {
			throw ErrorCode.BAD_REQUEST.exception("취소는 결제 완료 상태에서만 가능합니다.");
		}
		this.status = ReservationStatus.CANCELED;
	}

	public void markPaidCanceled() {
		if (this.status != ReservationStatus.CANCELED) {
			throw ErrorCode.BAD_REQUEST.exception("결제 취소는 취소 상태에서만 가능합니다.");
		}
		this.status = ReservationStatus.PAID_CANCELED;
	}

	public void markPaidErrorCanceled() {
		if (this.status != ReservationStatus.PAID_ERROR) {
			throw ErrorCode.BAD_REQUEST.exception("결제 에러 취소는 결제 에러 상태에서만 가능합니다.");
		}
		this.status = ReservationStatus.PAID_ERROR_CANCELED;
	}

	public void markPgCancelFail() {
		if (this.status != ReservationStatus.PAID_ERROR && this.status != ReservationStatus.CANCELED) {
			throw ErrorCode.BAD_REQUEST.exception("PG 결제 취소 실패는 결제 에러, 또는 취소 상태에서만 가능합니다.");
		}
		this.status = ReservationStatus.PG_CANCEL_FAIL;
	}
}

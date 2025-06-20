package com.reservation.domain.reservation.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
	PAID("결제 완료", "결제 및 검증 완료"),
	CONFIRMED("확정", "예약 확정"),

	PAID_ERROR("결제 에러", "미결제 혹은 결제 금액 불일치"),

	CANCELED("예약 취소", "취소 요청으로 인한 취소 상태"),

	PAID_ERROR_CANCELED("결제 취소", "결제 에러로 인한 결제 취소 상태"),
	PAID_CANCELED("결제 취소", "결제 취소 상태"),

	PG_VALIDATE_ERROR("PG 검증 실패", "PG사 결제 검증 요청 실패"),
	PG_CANCEL_FAIL("PG 결제 취소 실패", "PG사 결제 취소 요청 실패");

	private final String name;
	private final String description;

	ReservationStatus(String name, String description) {
		this.name = name;
		this.description = description;
	}
}

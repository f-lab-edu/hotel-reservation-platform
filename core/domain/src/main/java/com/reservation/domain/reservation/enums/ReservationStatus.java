package com.reservation.domain.reservation.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
	PENDING("결제 대기", "결제 대기 중"),
	PAID("결제 완료", "결제 및 검증 완료"),
	CONFIRMED("확정", "예약 확정"),

	EXPIRED("결제 시간 초과", "결제 시간 초과로 자동 무효"),
	PAID_ERROR("결제 에러", "미결제 혹은 결제 금액 불일치"),

	CUSTOMER_CANCELED("사용자 취소", "사용자 취소"),
	ADMIN_CANCELED("관리자 취소", "관리자 취소"),
	HOST_REJECTED("호스트 거절", "호스트 거절"),

	PAID_ERROR_CANCELED("결제 취소", "결제 에러로 인한 결제 취소"),
	CUSTOMER_PAID_CANCELED("결제 취소", "사용자 취소로 인한 결제 취소"),
	ADMIN_PAID_CANCELED("결제 취소", "관리자 취소로 인한 결제 취소"),
	HOST_PAID_CANCELED("결제 취소", "업체 거절로 인한 결제 취소"),

	PG_VALIDATE_ERROR("PG 검증 실패", "PG사 결제 검증 요청 실패"),
	PG_CANCEL_FAIL("PG 결제 취소 실패", "PG사 결제 취소 요청 실패");

	private final String name;
	private final String description;

	ReservationStatus(String name, String description) {
		this.name = name;
		this.description = description;
	}
}

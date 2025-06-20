package com.reservation.domain.reservation.enums;

import lombok.Getter;

@Getter
public enum ReservationEvents {
	// 결제 흐름
	VALIDATE_PAYMENT("결제 검증 요청", "PG에서 결제 금액을 조회하고 검증을 수행"),

	PAYMENT_SUCCESS("결제 성공", "결제 완료"),
	PAYMENT_FAILURE("결제 실패", "결제 금액 불일치"),
	PAYMENT_VALIDATE_ERROR("PG 검증 실패", "PG 통신 실패"),
	PAYMENT_EXPIRED("결제 만료", "결제 시간 초과"),

	// 체크인 흐름
	CHECKIN_PASSED("체크인 경과", "체크인 날짜 도래"),

	// 취소 요청
	CUSTOMER_CANCEL("사용자 취소 요청", "결제 전 사용자 취소"),
	ADMIN_CANCEL("관리자 취소 요청", "결제 전 관리자 취소"),
	HOST_REJECT("호스트 거절", "결제 전 호스트 거절"),

	// 결제 취소 요청
	CUSTOMER_PAYMENT_CANCEL("사용자 결제 취소 요청", "결제 후 사용자 취소"),
	ADMIN_PAYMENT_CANCEL("관리자 결제 취소 요청", "결제 후 관리자 취소"),
	HOST_PAYMENT_CANCEL("호스트 결제 취소 요청", "결제 후 호스트 거절"),

	PG_PAID_CANCEL("PG 결제 취소 요청", "PG사 결제 취소 요청"),

	// PG 실패
	PG_CANCEL_FAIL("PG 취소 실패", "결제 취소 요청 실패");

	private final String name;
	private final String description;

	ReservationEvents(String name, String description) {
		this.name = name;
		this.description = description;
	}
}

package com.reservation.domain.reservation.enums;

public enum ReservationStatus {
	PENDING,     // 결제 대기 중
	CONFIRMED,   // 결제 완료 및 예약 확정
	CANCELED,    // 사용자 취소 or 결제 실패
	EXPIRED      // 결제 시간 초과로 자동 무효
}

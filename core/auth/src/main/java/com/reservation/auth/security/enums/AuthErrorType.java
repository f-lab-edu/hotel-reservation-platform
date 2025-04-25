package com.reservation.auth.security.enums;

import com.reservation.support.response.ApiErrorResponse;

public enum AuthErrorType {
	MISSING_TOKEN("UNAUTHORIZED", "인증 토큰이 존재하지 않습니다."),
	MALFORMED_TOKEN("UNAUTHORIZED", "토큰 형식이 잘못되었습니다."),
	INVALID_TOKEN("UNAUTHORIZED", "토큰이 유효하지 않습니다."),
	BLACKLIST_TOKEN("UNAUTHORIZED", "사용 불가 인증 토큰 입니다.");

	private final String code;
	private final String message;

	AuthErrorType(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public ApiErrorResponse toResponse() {
		return ApiErrorResponse.of(this.code, this.message);
	}
}

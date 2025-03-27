package com.reservation.common.response;

public record ApiErrorResponse(
	boolean success,
	String code,
	String message,
	Object data // or null, or errors 리스트
) {
	public static ApiErrorResponse of(String code, String message) {
		return new ApiErrorResponse(false, code, message, null);
	}

	public static ApiErrorResponse of(String code, String message, Object data) {
		return new ApiErrorResponse(false, code, message, data);
	}
}

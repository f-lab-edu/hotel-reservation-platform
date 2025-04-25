package com.reservation.support.response;

public record ApiResponse<T>(
	boolean success,
	T data
) {
	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, data);
	}

	public static <T> ApiResponse<T> noContent() {
		return new ApiResponse<>(true, null);
	}
}


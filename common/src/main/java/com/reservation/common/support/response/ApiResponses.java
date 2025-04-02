package com.reservation.common.support.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.reservation.common.response.ApiSuccessResponse;

public class ApiResponses {
	public static <T> ResponseEntity<ApiSuccessResponse<T>> ok(T data) {
		return ResponseEntity.ok(ApiSuccessResponse.of(data));
	}

	public static <T> ResponseEntity<ApiSuccessResponse<T>> created(T data) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.of(data));
	}

	public static <T> ResponseEntity<ApiSuccessResponse<T>> noContent() {
		return ResponseEntity.ok(ApiSuccessResponse.of(null));
	}
}

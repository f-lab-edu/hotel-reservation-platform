package com.reservation.common.support.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.reservation.common.response.ApiSuccessResponse;

public class ApiResponses {
	public static <T> ApiSuccessResponse<T> ok(T data) {
		return ApiSuccessResponse.of(data);
	}

	public static <T> ResponseEntity<ApiSuccessResponse<T>> created(T data) {
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponse<>(true, data));
	}

	public static ResponseEntity<Void> noContent() {
		return ResponseEntity.noContent().build();
	}
}

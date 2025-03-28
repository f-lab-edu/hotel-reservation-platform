package com.reservation.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
	private final ErrorCode errorCode;

	protected BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ErrorCode errorCode() {
		return errorCode;
	}

	public HttpStatus status() {
		return errorCode.status();
	}
}

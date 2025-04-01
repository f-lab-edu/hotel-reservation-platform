package com.reservation.common.exception;

import static com.reservation.common.response.ApiErrorResponse.*;
import static com.reservation.common.response.ApiErrorResponse.of;
import static org.springframework.http.ResponseEntity.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.reservation.common.response.ApiErrorResponse;

import lombok.extern.log4j.Log4j2;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
	private static final String DEFAULT_ERROR_MESSAGE = "서버 내부 오류로 인한 작업 실패";

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ApiErrorResponse> handleValidationException(
		MethodArgumentNotValidException methodArgumentNotValidException) {

		List<FieldError> fieldErrors = methodArgumentNotValidException.getBindingResult().getFieldErrors();
		String message = fieldErrors.stream()
			.map(error -> String.format("[%s] %s", error.getField(), error.getDefaultMessage()))
			.collect(Collectors.joining(" | "));

		String responseCode = ErrorCode.VALIDATION_ERROR.name();
		ApiErrorResponse response = of(responseCode, message);

		return status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException businessException) {
		HttpStatus status = businessException.errorCode().status();
		String responseCode = businessException.errorCode().name();
		ApiErrorResponse response = of(responseCode, businessException.getMessage());

		return status(status).body(response);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
		log.error(exception);

		String responseCode = ErrorCode.INTERNAL_SERVER_ERROR.name();
		HttpStatus status = ErrorCode.INTERNAL_SERVER_ERROR.status();
		ApiErrorResponse response = of(responseCode, DEFAULT_ERROR_MESSAGE);

		return status(status).body(response);
	}
}

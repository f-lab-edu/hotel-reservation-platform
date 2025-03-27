package com.reservation.common.exception;

import static com.reservation.common.response.ApiErrorResponse.*;
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

@RestControllerAdvice
public class GlobalExceptionHandler {
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
		String responseCode = ErrorCode.INTERNAL_SERVER_ERROR.name();
		HttpStatus status = ErrorCode.VALIDATION_ERROR.status();
		ApiErrorResponse response = of(responseCode, exception.getMessage());

		return status(status).body(response);
	}
}

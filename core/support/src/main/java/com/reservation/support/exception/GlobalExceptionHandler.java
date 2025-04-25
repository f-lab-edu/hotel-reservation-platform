package com.reservation.support.exception;

import static org.springframework.http.ResponseEntity.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.reservation.support.response.ApiErrorResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final String NOT_FOUND_ERROR_MESSAGE = "존재하지 않는 URL입니다";
	private static final String DEFAULT_ERROR_MESSAGE = "서버 내부 오류로 인한 작업 실패";

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ApiErrorResponse> handle404(NoHandlerFoundException e) {
		ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.NOT_FOUND.name(), NOT_FOUND_ERROR_MESSAGE);
		return status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handle404(HttpMessageNotReadableException e) {
		ApiErrorResponse response =
			ApiErrorResponse.of(ErrorCode.BAD_REQUEST.name(), e.fillInStackTrace().getMessage());
		return status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationException(
		MethodArgumentNotValidException methodArgumentNotValidException) {

		List<FieldError> fieldErrors = methodArgumentNotValidException.getBindingResult().getFieldErrors();
		String message = fieldErrors.stream()
			.map(error -> String.format("[%s] %s", error.getField(), error.getDefaultMessage()))
			.collect(Collectors.joining(" | "));

		String responseCode = ErrorCode.VALIDATION_ERROR.name();
		ApiErrorResponse response = ApiErrorResponse.of(responseCode, message);

		return status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException businessException) {
		HttpStatus status = businessException.errorCode().status();
		String responseCode = businessException.errorCode().name();
		ApiErrorResponse response = ApiErrorResponse.of(responseCode, businessException.getMessage());

		return status(status).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
		log.error(exception.getMessage(), exception);
		ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR.name(), DEFAULT_ERROR_MESSAGE);
		return status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}

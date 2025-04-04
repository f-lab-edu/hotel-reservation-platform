package com.reservation.common.exception;

import static com.reservation.common.response.ApiErrorResponse.*;
import static com.reservation.common.response.ApiErrorResponse.of;
import static org.springframework.http.ResponseEntity.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.reservation.common.response.ApiErrorResponse;

import lombok.extern.log4j.Log4j2;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
	private static final String DEFAULT_ERROR_MESSAGE = "서버 내부 오류로 인한 작업 실패";

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(
		HttpMessageNotReadableException httpMessageNotReadableException) {
		Throwable cause = httpMessageNotReadableException.getCause();

		if (cause instanceof InvalidFormatException invalidFormatException) {
			String fieldName = invalidFormatException.getPath().stream()
				.map(JsonMappingException.Reference::getFieldName)
				.collect(Collectors.joining("."));

			String targetType = invalidFormatException.getTargetType().getSimpleName();
			String invalidValue = String.valueOf(invalidFormatException.getValue());

			String message = String.format("'%s' 필드에 잘못된 값 '%s'이(가) 전달되었습니다. (%s 타입 필요)",
				fieldName, invalidValue, targetType);

			ApiErrorResponse response = of(ErrorCode.VALIDATION_ERROR.name(), message);
			return ResponseEntity.badRequest().body(response);
		}

		// 기타 경우는 단순 메시지 출력
		ApiErrorResponse response = of(ErrorCode.VALIDATION_ERROR.name(), "요청 본문이 잘못되었습니다.");
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleEnumTypeMismatch(
		MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
		String param = methodArgumentTypeMismatchException.getName();
		Object value = methodArgumentTypeMismatchException.getValue();
		String invalidValue = value != null ? value.toString() : "null";

		System.out.println(methodArgumentTypeMismatchException.getMessage());

		String responseCode = ErrorCode.VALIDATION_ERROR.name();
		String message = String.format("'%s' 파라미터에 잘못된 값 '%s'이(가) 전달되었습니다.", param, invalidValue);
		ApiErrorResponse response = of(responseCode, message);

		return badRequest().body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationException(
		MethodArgumentNotValidException methodArgumentNotValidException) {

		System.out.println(methodArgumentNotValidException.getMessage());

		List<FieldError> fieldErrors = methodArgumentNotValidException.getBindingResult().getFieldErrors();
		String message = fieldErrors.stream()
			.map(error -> String.format("[%s] %s", error.getField(), error.getDefaultMessage()))
			.collect(Collectors.joining(" | "));

		String responseCode = ErrorCode.VALIDATION_ERROR.name();
		ApiErrorResponse response = of(responseCode, message);

		return badRequest().body(response);
	}

	@ExceptionHandler(ConversionFailedException.class)
	public ResponseEntity<ApiErrorResponse> handleConversionFailed(
		ConversionFailedException conversionFailedException) {
		String message = String.format("파라미터 타입 변환 실패: '%s'", conversionFailedException.getValue());
		ApiErrorResponse response = of(ErrorCode.VALIDATION_ERROR.name(), message);
		return ResponseEntity.badRequest().body(response);
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

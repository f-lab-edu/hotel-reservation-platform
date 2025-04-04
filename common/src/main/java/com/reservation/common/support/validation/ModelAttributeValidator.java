package com.reservation.common.support.validation;

import java.util.stream.Collectors;

import org.springframework.validation.BindingResult;

import com.reservation.common.exception.ErrorCode;

public class ModelAttributeValidator {
	public static void validate(BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			String errorMessage = bindingResult.getFieldErrors().stream()
				.map(error -> {
					String fieldName = error.getField();
					String message = error.getDefaultMessage();
					if (message != null && message.contains("Failed to convert")) {
						message = "잘못된 enum 값입니다.";
					}
					return String.format("[%s] %s", fieldName, message);
				})
				.collect(Collectors.joining(" | "));

			throw ErrorCode.VALIDATION_ERROR.exception(errorMessage);
		}
	}
}

package com.reservation.common.support.validation;

import java.util.stream.Collectors;

import org.springframework.validation.BindingResult;

import com.reservation.common.exception.ErrorCode;

public class ModelAttributeValidator {
	public static void validate(BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			String errorMessage = bindingResult.getFieldErrors().stream()
				.map(error -> String.format("[%s] %s", error.getField(), error.getDefaultMessage()))
				.collect(Collectors.joining(" | "));

			throw ErrorCode.VALIDATION_ERROR.exception(errorMessage);
		}
	}
}

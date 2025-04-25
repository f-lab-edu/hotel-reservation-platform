package com.reservation.auth.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.auth.security.enums.AuthErrorType;
import com.reservation.support.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException) throws IOException {

		AuthErrorType errorType = Optional.ofNullable((AuthErrorType)request.getAttribute("authError"))
			.orElse(AuthErrorType.INVALID_TOKEN); // 기본 값 설정

		ApiErrorResponse responseBody = errorType.toResponse();

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		String json = new ObjectMapper().writeValueAsString(responseBody);
		response.getWriter().write(json);
	}
}

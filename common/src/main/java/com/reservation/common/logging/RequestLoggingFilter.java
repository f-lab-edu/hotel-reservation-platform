package com.reservation.common.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {

		// 1. Trace ID 생성
		String traceId = UUID.randomUUID().toString().substring(0, 8);
		MDC.put("traceId", traceId); // 로그에 traceId 자동 삽입
		long startTime = System.currentTimeMillis();

		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

		try {
			// 2. 요청 정보 로그
			log.info("➡️ [{}] {} {}", getClientIp(request), request.getMethod(), request.getRequestURI());

			chain.doFilter(wrappedRequest, wrappedResponse);

		} finally {
			long duration = System.currentTimeMillis() - startTime;

			// 3. 요청 바디 로그
			String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);

			// 4. 응답 바디 로그
			String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
			wrappedResponse.copyBodyToResponse(); // body 재전송!

			log.info("⬅️ [{}] {} {} ({}ms)\n📝 요청 바디: {}\n📦 응답 바디: {}",
				getClientIp(request),
				request.getMethod(),
				request.getRequestURI(),
				duration,
				requestBody,
				responseBody
			);

			MDC.clear();
		}
	}

	private String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isBlank()) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}

package com.reservation.auth.token;

import static org.springframework.context.annotation.ScopedProxyMode.*;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequestScope(proxyMode = TARGET_CLASS)
@RequiredArgsConstructor
public class RequestContext {
	public static final String REFRESH_COOKIE_NAME = "refreshToken";
	public static final String AUTH_HEADER_NAME = "Authorization";
	public static final String AUTH_HEADER_PREFIX = "Bearer ";

	private final HttpServletRequest request;

	public String getRefreshToken() {
		return getCookie(REFRESH_COOKIE_NAME);
	}

	public String getCookie(String name) {
		if (request.getCookies() == null) {
			return null;
		}

		return Arrays.stream(request.getCookies())
			.filter(c -> c.getName().equals(name))
			.map(Cookie::getValue)
			.findFirst()
			.orElse(null);
	}

	public String getAccessToken() {
		String authHeader = request.getHeader(AUTH_HEADER_NAME);
		if (authHeader == null || !authHeader.startsWith(AUTH_HEADER_PREFIX)) {
			return null;
		}
		return authHeader.substring(AUTH_HEADER_PREFIX.length());
	}
}

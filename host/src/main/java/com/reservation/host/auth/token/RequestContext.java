package com.reservation.host.auth.token;

import static com.reservation.host.auth.controller.AuthController.*;
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
}

package com.reservation.commonauth.auth.login;

import static com.reservation.commonauth.auth.token.RequestContext.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.reservation.commonmodel.auth.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class LogoutService {
	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

	private final RedisTemplate<String, String> redisTemplate;
	private final BlacklistService blacklistService;

	public ResponseEntity<Void> logout(Long userId, Role role) {
		deleteRefreshToken(userId, role);

		blacklistService.setBlacklistToken(userId, role);

		ResponseCookie deleteCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(0)
			.build();

		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
			.build();
	}

	private void deleteRefreshToken(Long userId, Role role) {
		String refreshTokenKey = REFRESH_TOKEN_PREFIX + role.name().toLowerCase() + ":" + userId;
		redisTemplate.delete(refreshTokenKey);
	}
}

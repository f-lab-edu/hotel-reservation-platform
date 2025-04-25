package com.reservation.auth.login;

import static com.reservation.auth.token.RequestContext.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class LogoutService {
	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

	private final RedisTemplate<String, String> redisTemplate;
	private final BlacklistService blacklistService;

	public String logout(Long userId, Role role) {
		deleteRefreshToken(userId, role);

		blacklistService.setBlacklistToken(userId, role);

		return REFRESH_COOKIE_NAME;
	}

	private void deleteRefreshToken(Long userId, Role role) {
		String refreshTokenKey = REFRESH_TOKEN_PREFIX + role.name().toLowerCase() + ":" + userId;
		redisTemplate.delete(refreshTokenKey);
	}
}

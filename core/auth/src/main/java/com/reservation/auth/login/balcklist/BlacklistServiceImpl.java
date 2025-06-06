package com.reservation.auth.login.balcklist;

import java.time.Duration;
import java.util.Date;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.reservation.auth.login.BlacklistService;
import com.reservation.auth.token.JwtTokenProvider;
import com.reservation.auth.token.RequestContext;
import com.reservation.support.enums.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {
	public static final String BLACKLIST_TOKEN_PREFIX = "blacklist_token:";

	private final RedisTemplate<String, String> redisTemplate;
	private final JwtTokenProvider jwtTokenProvider;
	private final RequestContext requestContext;

	public void setBlacklistToken(Long userId, Role role) {
		String accessToken = requestContext.getAccessToken();
		if (accessToken == null) {
			return;
		}

		Date expiration = jwtTokenProvider.extractExpiration(accessToken);
		if (expiration == null) {
			return;
		}
		Duration duration = Duration.ofMinutes(expiration.getTime());

		String blacklistKey = BLACKLIST_TOKEN_PREFIX + accessToken;
		String value = role.name().toLowerCase() + ":" + userId.toString();

		redisTemplate.opsForValue().set(blacklistKey, value, duration);
	}

	public boolean checkBlacklistToken(String accessToken) {
		return redisTemplate.hasKey(BLACKLIST_TOKEN_PREFIX + accessToken);
	}
}

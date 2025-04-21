package com.reservation.commonauth.auth.login;

import java.time.Duration;
import java.util.Date;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.reservation.commonauth.auth.token.JwtTokenProvider;
import com.reservation.commonauth.auth.token.RequestContext;
import com.reservation.commonmodel.auth.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class BlacklistService {
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
}

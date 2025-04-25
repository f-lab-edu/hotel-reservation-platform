package com.reservation.auth.login;

import static com.reservation.auth.token.RequestContext.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.reservation.auth.login.dto.AccessTokenHeader;
import com.reservation.auth.token.JwtTokenProvider;
import com.reservation.auth.token.RequestContext;
import com.reservation.support.enums.Role;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshService {
	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;
	private final RequestContext requestContext;
	private final BlacklistService blacklistService;

	public AccessTokenHeader tokenReissue(Long userId, Role role) {
		String key = REFRESH_TOKEN_PREFIX + role.name().toLowerCase() + ":" + userId;
		String redisToken = redisTemplate.opsForValue().get(key);

		if (redisToken == null || redisToken.isBlank()) {
			throw ErrorCode.UNAUTHORIZED.exception("로그인 정보가 만료되었습니다.");
		}

		String refreshToken = requestContext.getRefreshToken();
		if (refreshToken == null || refreshToken.isBlank()) {
			throw ErrorCode.UNAUTHORIZED.exception("로그인 정보가 만료되었습니다.");
		}

		if (!redisToken.equals(refreshToken)) {
			throw ErrorCode.UNAUTHORIZED.exception("인증 정보가 일치하지 않습니다.");
		}

		blacklistService.setBlacklistToken(userId, role);

		String accessToken = jwtTokenProvider.generateToken(userId, role.authority());
		return new AccessTokenHeader(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + accessToken);
	}
}

package com.reservation.auth.login;

import static com.reservation.auth.token.RequestContext.*;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.reservation.auth.config.JwtProperties;
import com.reservation.auth.login.dto.AccessTokenHeader;
import com.reservation.auth.login.dto.LoginSettingToken;
import com.reservation.auth.login.dto.RefreshTokenCookie;
import com.reservation.auth.token.JwtTokenProvider;
import com.reservation.support.enums.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {
	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

	private final JwtProperties jwtProperties;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;
	private final BlacklistService blacklistService;

	public LoginSettingToken login(Long userId, Role role) {
		blacklistService.setBlacklistToken(userId, role);

		String refreshToken = storeRefreshToken(userId, role);

		AccessTokenHeader accessTokenHeader = new AccessTokenHeader(AUTH_HEADER_NAME,
			AUTH_HEADER_PREFIX + jwtTokenProvider.generateToken(userId, role.authority()));

		RefreshTokenCookie refreshTokenCookie = new RefreshTokenCookie(REFRESH_COOKIE_NAME, refreshToken,
			Duration.ofMillis(jwtProperties.getRefreshTokenExpiry()));

		return new LoginSettingToken(accessTokenHeader, refreshTokenCookie);
	}

	public String storeRefreshToken(Long userId, Role role) {
		String refreshToken = jwtTokenProvider.generateRefreshToken(userId, role.authority());
		String key = REFRESH_TOKEN_PREFIX + role.name().toLowerCase() + ":" + userId;
		redisTemplate.opsForValue()
			.set(key, refreshToken, Duration.ofMillis(jwtProperties.getRefreshTokenExpiry()));

		return refreshToken;
	}
}

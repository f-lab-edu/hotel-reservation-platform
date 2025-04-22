package com.reservation.commonauth.auth.login;

import static com.reservation.commonauth.auth.token.RequestContext.*;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.reservation.commonauth.auth.config.JwtProperties;
import com.reservation.commonauth.auth.token.JwtTokenProvider;
import com.reservation.commonmodel.auth.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class LoginService {
	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

	private final JwtProperties jwtProperties;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;
	private final BlacklistService blacklistService;

	public ResponseEntity<Void> login(Long userId, Role role) {
		blacklistService.setBlacklistToken(userId, role);

		ResponseCookie refreshTokenCookie = storeRefreshToken(userId, role);

		return ResponseEntity.noContent()
			.header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + jwtTokenProvider.generateToken(userId, role.authority()))
			.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
			.build();
	}

	public ResponseCookie storeRefreshToken(Long userId, Role role) {
		String refreshToken = jwtTokenProvider.generateRefreshToken(userId, role.authority());
		String key = REFRESH_TOKEN_PREFIX + role.name().toLowerCase() + ":" + userId;
		redisTemplate.opsForValue()
			.set(key, refreshToken, Duration.ofMillis(jwtProperties.getRefreshTokenExpiry()));

		return ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpiry()))
			.build();
	}

	public ResponseEntity<Void> login(Long userId, Role role, String redirectUrl) {
		blacklistService.setBlacklistToken(userId, role);

		ResponseCookie refreshTokenCookie = storeRefreshToken(userId, role);

		return ResponseEntity.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, redirectUrl)
			.header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + jwtTokenProvider.generateToken(userId, role.authority()))
			.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
			.build();
	}
}

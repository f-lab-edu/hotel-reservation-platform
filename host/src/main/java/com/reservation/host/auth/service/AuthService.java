package com.reservation.host.auth.service;

import static com.reservation.commonauth.auth.token.JwtTokenProvider.*;
import static com.reservation.host.auth.controller.AuthController.*;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reservation.commonapi.host.repository.HostModuleRepository;
import com.reservation.commonauth.auth.token.JwtTokenProvider;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.host.HostDto;
import com.reservation.commonmodel.host.HostStatus;
import com.reservation.host.auth.controller.dto.request.LoginRequest;
import com.reservation.host.auth.service.dto.LoginDto;
import com.reservation.host.auth.token.RequestContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {
	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:host:";
	private final HostModuleRepository hostRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;
	private final PasswordEncoder passwordEncoder;
	private final RequestContext requestContext;

	public LoginDto login(LoginRequest request) {
		HostDto hostDto = hostRepository.findOneByEmailAndStatusIsNot(request.email(), HostStatus.WITHDRAWN)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("이메일 정보가 존재하지 않습니다."));
		if (hostDto.status() == HostStatus.SUSPENDED) {
			throw ErrorCode.NOT_FOUND.exception("정지 계정 입니다. 고객 센터로 연락 바랍니다.");
		}
		if (!passwordEncoder.matches(request.password(), hostDto.password())) {
			throw ErrorCode.NOT_FOUND.exception("로그인 정보가 일치하지 않습니다.");
		}

		String accessToken = jwtTokenProvider.generateToken(hostDto.id(), ROLE_HOST);
		String refreshToken = storeRefreshToken(hostDto.id());

		return new LoginDto(hostDto.id(), accessToken, refreshToken);
	}

	private String storeRefreshToken(Long hostId) {
		String refreshToken = jwtTokenProvider.generateRefreshToken(hostId, ROLE_HOST);
		String key = REFRESH_TOKEN_PREFIX + hostId;
		redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMinutes(REFRESH_TOKEN_VALIDITY_IN_MILLIS));
		return refreshToken;
	}

	public HostDto findMe(Long hostId) {
		return hostRepository.findById(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("호스트 정보를 찾을 수 없습니다."));
	}

	public String tokenReissue(Long hostId) {
		String key = REFRESH_TOKEN_PREFIX + hostId;
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

		return jwtTokenProvider.generateToken(hostId, ROLE_HOST);
	}

	public void logout(Long hostId) {
		String key = REFRESH_TOKEN_PREFIX + hostId;
		redisTemplate.delete(key);
	}
}

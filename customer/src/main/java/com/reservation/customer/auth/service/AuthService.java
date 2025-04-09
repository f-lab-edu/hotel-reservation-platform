package com.reservation.customer.auth.service;

import static com.reservation.commonauth.auth.token.JwtTokenProvider.*;
import static com.reservation.customer.auth.controller.AuthController.*;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reservation.commonapi.customer.repository.CustomerMemberRepository;
import com.reservation.commonauth.auth.token.JwtTokenProvider;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.member.MemberDto;
import com.reservation.commonmodel.member.MemberStatus;
import com.reservation.customer.auth.controller.dto.request.LoginRequest;
import com.reservation.customer.auth.service.dto.LoginDto;
import com.reservation.customer.auth.token.RequestContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {
	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:customer:";
	private final CustomerMemberRepository memberRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;
	private final PasswordEncoder passwordEncoder;
	private final RequestContext requestContext;

	public LoginDto login(LoginRequest request) {
		MemberDto memberDto = memberRepository.findOneByEmailAndStatusIsNot(request.email(), MemberStatus.WITHDRAWN);
		if (memberDto.status() == MemberStatus.INACTIVE) {
			throw ErrorCode.NOT_FOUND.exception("휴먼 계정 입니다. 휴먼 해제 바랍니다.");
		}
		if (memberDto.status() == MemberStatus.SUSPENDED) {
			throw ErrorCode.NOT_FOUND.exception("정지 계정 입니다. 고객 센터로 연락 바랍니다.");
		}
		if (!passwordEncoder.matches(request.password(), memberDto.password())) {
			throw ErrorCode.NOT_FOUND.exception("로그인 정보가 일치하지 않습니다.");
		}

		String accessToken = jwtTokenProvider.generateToken(memberDto.id(), ROLE_CUSTOMER);
		String refreshToken = storeRefreshToken(memberDto.id());

		return new LoginDto(memberDto.id(), accessToken, refreshToken);
	}

	private String storeRefreshToken(Long memberId) {
		String refreshToken = jwtTokenProvider.generateRefreshToken(memberId, ROLE_CUSTOMER);
		String key = REFRESH_TOKEN_PREFIX + memberId;
		redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMinutes(REFRESH_TOKEN_VALIDITY_IN_MILLIS));
		return refreshToken;
	}

	public MemberDto findMe(Long memberId) {
		return memberRepository.findById(memberId);
	}

	public String tokenReissue(Long memberId) {
		String key = REFRESH_TOKEN_PREFIX + memberId;
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

		return jwtTokenProvider.generateToken(memberId, ROLE_CUSTOMER);
	}

	public void logout(Long memberId) {
		String key = REFRESH_TOKEN_PREFIX + memberId;
		redisTemplate.delete(key);
	}
}

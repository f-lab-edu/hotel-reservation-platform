package com.reservation.customer.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.reservation.commonapi.customer.repository.CustomerMemberRepository;
import com.reservation.commonauth.auth.token.JwtTokenProvider;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.member.MemberDto;
import com.reservation.commonmodel.member.MemberStatus;
import com.reservation.customer.auth.controller.dto.request.LoginRequest;
import com.reservation.customer.auth.service.dto.LoginDto;
import com.reservation.customer.auth.token.RequestContext;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:customer:";
	private static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private RequestContext requestContext;

	@InjectMocks
	private AuthService authService;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private CustomerMemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	private LoginRequest loginRequest;
	private MemberDto memberDto;

	@BeforeEach
	void setUp() {
		loginRequest = new LoginRequest("test@example.com", "password123");
		memberDto = new MemberDto(
			1L,
			"encryptedPassword",
			MemberStatus.ACTIVE,
			"test@example.com",
			"01099999999",
			LocalDateTime.now(),
			LocalDateTime.now(),
			null);
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	@DisplayName("로그인 성공")
	void login_Success() {
		when(memberRepository.findOneByEmailAndStatusIsNot(loginRequest.email(), MemberStatus.WITHDRAWN))
			.thenReturn(memberDto);
		when(passwordEncoder.matches(loginRequest.password(), memberDto.password())).thenReturn(true);
		when(jwtTokenProvider.generateToken(memberDto.id(), "ROLE_CUSTOMER")).thenReturn("accessToken");
		when(jwtTokenProvider.generateRefreshToken(memberDto.id(), "ROLE_CUSTOMER")).thenReturn("refreshToken");

		LoginDto loginDto = authService.login(loginRequest);

		assertThat(loginDto).isNotNull();
		assertThat(loginDto.accessToken()).isEqualTo("accessToken");
		assertThat(loginDto.refreshToken()).isEqualTo("refreshToken");
		verify(redisTemplate.opsForValue()).set(
			eq("refresh_token:customer:1"),
			eq("refreshToken"),
			eq(Duration.ofMinutes(JwtTokenProvider.REFRESH_TOKEN_VALIDITY_IN_MILLIS))
		);
	}

	@Test
	@DisplayName("로그인 실패 - 비밀번호 불일치")
	void login_Fail_InvalidPassword() {
		when(memberRepository.findOneByEmailAndStatusIsNot(loginRequest.email(), MemberStatus.WITHDRAWN))
			.thenReturn(memberDto);
		when(passwordEncoder.matches(loginRequest.password(), memberDto.password())).thenReturn(false);

		BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(loginRequest));

		assertThat(exception.getMessage()).isEqualTo("로그인 정보가 일치하지 않습니다.");
	}

	@Test
	@DisplayName("로그인 실패 - 휴먼 계정")
	void login_Fail_InactiveAccount() {
		memberDto = new MemberDto(
			1L,
			"encryptedPassword",
			MemberStatus.INACTIVE,
			"test@example.com",
			"01099999999",
			LocalDateTime.now(),
			LocalDateTime.now(),
			null);
		when(memberRepository.findOneByEmailAndStatusIsNot(loginRequest.email(), MemberStatus.WITHDRAWN))
			.thenReturn(memberDto);

		BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(loginRequest));

		assertThat(exception.getMessage()).isEqualTo("휴먼 계정 입니다. 휴먼 해제 바랍니다.");
	}

	@Test
	@DisplayName("로그인 실패 - 정지 계정")
	void login_Fail_SuspendedAccount() {
		memberDto = new MemberDto(
			1L,
			"encryptedPassword",
			MemberStatus.SUSPENDED,
			"test@example.com",
			"01099999999",
			LocalDateTime.now(),
			LocalDateTime.now(),
			null);
		when(memberRepository.findOneByEmailAndStatusIsNot(loginRequest.email(), MemberStatus.WITHDRAWN))
			.thenReturn(memberDto);

		BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(loginRequest));

		assertThat(exception.getMessage()).isEqualTo("정지 계정 입니다. 고객 센터로 연락 바랍니다.");
	}

	@Test
	@DisplayName("회원 정보 조회 성공")
	void findMe_Success() {
		when(memberRepository.findById(1L)).thenReturn(memberDto);

		MemberDto result = authService.findMe(1L);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.email()).isEqualTo("test@example.com");
	}

	@Test
	@DisplayName("회원 정보 조회 실패 - 존재하지 않는 회원")
	void findMe_Fail_MemberNotFound() {
		when(memberRepository.findById(1L)).thenThrow(ErrorCode.NOT_FOUND.exception("회원 정보가 존재하지 않습니다."));

		BusinessException exception = assertThrows(BusinessException.class, () -> authService.findMe(1L));

		assertThat(exception.getMessage()).isEqualTo("회원 정보가 존재하지 않습니다.");
	}

	@Test
	@DisplayName("토큰 재발급 성공")
	void tokenReissue_Success() {
		Long memberId = 1L;
		String key = REFRESH_TOKEN_PREFIX + memberId;
		String redisToken = "validRefreshToken";
		String newAccessToken = "newAccessToken";

		when(valueOperations.get(key)).thenReturn(redisToken);
		when(requestContext.getRefreshToken()).thenReturn(redisToken);
		when(jwtTokenProvider.generateToken(memberId, ROLE_CUSTOMER)).thenReturn(newAccessToken);

		String result = authService.tokenReissue(memberId);

		assertThat(result).isEqualTo(newAccessToken);
		verify(valueOperations).get(key);
		verify(requestContext).getRefreshToken();
		verify(jwtTokenProvider).generateToken(memberId, ROLE_CUSTOMER);
	}

	@Test
	@DisplayName("토큰 재발급 실패 - Redis에 토큰 없음")
	void tokenReissue_Fail_TokenNotInRedis() {
		Long memberId = 1L;
		String key = REFRESH_TOKEN_PREFIX + memberId;

		when(valueOperations.get(key)).thenReturn(null);

		BusinessException exception = assertThrows(BusinessException.class, () -> authService.tokenReissue(memberId));

		assertThat(exception.getMessage()).isEqualTo("로그인 정보가 만료되었습니다.");
		verify(valueOperations).get(key);
	}

	@Test
	@DisplayName("토큰 재발급 실패 - RequestContext에 토큰 없음")
	void tokenReissue_Fail_TokenNotInRequestContext() {
		Long memberId = 1L;
		String key = REFRESH_TOKEN_PREFIX + memberId;
		String redisToken = "validRefreshToken";

		when(valueOperations.get(key)).thenReturn(redisToken);
		when(requestContext.getRefreshToken()).thenReturn(null);

		BusinessException exception = assertThrows(BusinessException.class, () -> authService.tokenReissue(memberId));

		assertThat(exception.getMessage()).isEqualTo("로그인 정보가 만료되었습니다.");
		verify(valueOperations).get(key);
		verify(requestContext).getRefreshToken();
	}

	@Test
	@DisplayName("토큰 재발급 실패 - Redis 토큰과 RequestContext 토큰 불일치")
	void tokenReissue_Fail_TokenMismatch() {
		Long memberId = 1L;
		String key = REFRESH_TOKEN_PREFIX + memberId;
		String redisToken = "validRefreshToken";
		String requestToken = "invalidRefreshToken";

		when(valueOperations.get(key)).thenReturn(redisToken);
		when(requestContext.getRefreshToken()).thenReturn(requestToken);

		BusinessException exception = assertThrows(BusinessException.class, () -> authService.tokenReissue(memberId));

		assertThat(exception.getMessage()).isEqualTo("인증 정보가 일치하지 않습니다.");
		verify(valueOperations).get(key);
		verify(requestContext).getRefreshToken();
	}

	@Test
	@DisplayName("로그아웃 성공 - RefreshToken이 존재하는 경우")
	void logout_Success_WithRefreshToken() {
		Long memberId = 1L;
		String key = REFRESH_TOKEN_PREFIX + memberId;
		String refreshToken = "validRefreshToken";

		when(redisTemplate.delete(key)).thenReturn(true);

		authService.logout(memberId);

		verify(redisTemplate).delete(key);
	}
}

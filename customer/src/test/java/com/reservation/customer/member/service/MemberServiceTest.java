package com.reservation.customer.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.commonapi.customer.repository.CustomerMemberRepository;
import com.reservation.commonapi.customer.repository.CustomerTermsRepository;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.commonmodel.member.MemberDto;
import com.reservation.commonmodel.member.MemberStatus;
import com.reservation.commonmodel.terms.ClauseDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;
import com.reservation.customer.member.controller.dto.request.SignupRequest;
import com.reservation.customer.phoneverification.service.dto.PhoneVerifiedRedisValue;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

	@Mock
	private CustomerMemberRepository memberRepository;

	@Mock
	private CustomerTermsRepository termsRepository;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private MemberService memberService;

	private SignupRequest signupRequest;
	private PhoneVerifiedRedisValue phoneVerifiedRedisValue;
	private ValueOperations<String, String> valueOperations;

	@BeforeEach
	void setUp() {
		signupRequest = new SignupRequest("010-1234-5678", "test@example.com", "password123");
		phoneVerifiedRedisValue = new PhoneVerifiedRedisValue(Set.of(1L, 2L));
		valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	@DisplayName("회원가입 성공")
	void signup_Success() throws JsonProcessingException {
		String phoneNumber = "01012345678";
		String key = "phone:verified:" + phoneNumber;

		when(valueOperations.get(key)).thenReturn("{\"agreedTermsIds\":[1,2]}");
		when(objectMapper.readValue(anyString(), eq(PhoneVerifiedRedisValue.class))).thenReturn(
			phoneVerifiedRedisValue);

		when(termsRepository.findByStatusAndIsLatest(TermsStatus.ACTIVE, true)).thenReturn(List.of(
			new TermsDto(
				1L,
				TermsCode.TERMS_USE,
				"서비스 이용약관",
				TermsType.REQUIRED,
				TermsStatus.ACTIVE,
				1,
				false, // isLatest가 false
				LocalDateTime.of(2025, 3, 25, 0, 0),
				LocalDateTime.of(2026, 3, 25, 0, 0),
				1,
				null,
				null,
				List.of(
					new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
					new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
				)
			),
			new TermsDto(
				2L,
				TermsCode.TERMS_USE,
				"서비스 이용약관",
				TermsType.REQUIRED,
				TermsStatus.ACTIVE,
				1,
				false, // isLatest가 false
				LocalDateTime.of(2025, 3, 25, 0, 0),
				LocalDateTime.of(2026, 3, 25, 0, 0),
				1,
				null,
				null,
				List.of(
					new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
					new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
				)
			)
		));

		when(memberRepository.existsByEmailAndStatus(signupRequest.email(), MemberStatus.ACTIVE)).thenReturn(false);
		when(memberRepository.existsByPhoneNumberAndStatus(phoneNumber, MemberStatus.ACTIVE)).thenReturn(false);

		when(passwordEncoder.encode(signupRequest.password())).thenReturn("encryptedPassword");

		memberService.signup(signupRequest);

		verify(memberRepository).save(any(MemberDto.class));
		verify(redisTemplate).delete(key);
	}

	@Test
	@DisplayName("핸드폰 인증 확인 실패")
	void signup_ThrowsExceptionWhenPhoneVerificationFails() {
		String phoneNumber = "01012345678";
		String key = "phone:verified:" + phoneNumber;

		// ValueOperations를 mock으로 설정
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		// Redis에서 key 조회 시 null 반환하도록 설정
		when(valueOperations.get(key)).thenReturn(null);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			memberService.signup(signupRequest);
		});

		assertThat(exception.getMessage()).isEqualTo("핸드폰 인증이 확인되지 않습니다. 다시 시도해주세요.");
	}

	@Test
	@DisplayName("필수 약관 동의 실패")
	void signup_ThrowsExceptionWhenRequiredTermsNotAgreed() throws JsonProcessingException {
		String phoneNumber = "01012345678";
		String key = "phone:verified:" + phoneNumber;

		when(valueOperations.get(key)).thenReturn("{\"agreedTermsIds\":[2]}");
		when(objectMapper.readValue(anyString(), eq(PhoneVerifiedRedisValue.class))).thenReturn(
			phoneVerifiedRedisValue);

		when(termsRepository.findByStatusAndIsLatest(TermsStatus.ACTIVE, true)).thenReturn(List.of(
			new TermsDto(
				1L,
				TermsCode.TERMS_USE,
				"서비스 이용약관",
				TermsType.REQUIRED,
				TermsStatus.ACTIVE,
				1,
				false, // isLatest가 false
				LocalDateTime.of(2025, 3, 25, 0, 0),
				LocalDateTime.of(2026, 3, 25, 0, 0),
				1,
				null,
				null,
				List.of(
					new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
					new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
				)
			),
			new TermsDto(
				3L,
				TermsCode.TERMS_USE,
				"서비스 이용약관",
				TermsType.REQUIRED,
				TermsStatus.ACTIVE,
				1,
				false, // isLatest가 false
				LocalDateTime.of(2025, 3, 25, 0, 0),
				LocalDateTime.of(2026, 3, 25, 0, 0),
				1,
				null,
				null,
				List.of(
					new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
					new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
				)
			)
		));

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			memberService.signup(signupRequest);
		});

		assertThat(exception.getMessage()).isEqualTo("필수 약관 정보에 동의하지 않았습니다.");
	}

	@Test
	@DisplayName("중복 이메일로 인한 회원가입 실패")
	void signup_ThrowsExceptionWhenEmailAlreadyExists() throws JsonProcessingException {
		String phoneNumber = "01012345678";
		String key = "phone:verified:" + phoneNumber;

		when(valueOperations.get(key)).thenReturn("{\"agreedTermsIds\":[1,2]}");
		when(objectMapper.readValue(anyString(), eq(PhoneVerifiedRedisValue.class))).thenReturn(
			phoneVerifiedRedisValue);

		when(termsRepository.findByStatusAndIsLatest(TermsStatus.ACTIVE, true)).thenReturn(List.of(
			new TermsDto(
				1L,
				TermsCode.TERMS_USE,
				"서비스 이용약관",
				TermsType.REQUIRED,
				TermsStatus.ACTIVE,
				1,
				false, // isLatest가 false
				LocalDateTime.of(2025, 3, 25, 0, 0),
				LocalDateTime.of(2026, 3, 25, 0, 0),
				1,
				null,
				null,
				List.of(
					new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
					new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
				)
			),
			new TermsDto(
				2L,
				TermsCode.TERMS_USE,
				"서비스 이용약관",
				TermsType.REQUIRED,
				TermsStatus.ACTIVE,
				1,
				false, // isLatest가 false
				LocalDateTime.of(2025, 3, 25, 0, 0),
				LocalDateTime.of(2026, 3, 25, 0, 0),
				1,
				null,
				null,
				List.of(
					new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
					new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
				)
			)
		));

		when(memberRepository.existsByEmailAndStatus(signupRequest.email(), MemberStatus.ACTIVE)).thenReturn(true);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			memberService.signup(signupRequest);
		});

		assertThat(exception.getMessage()).isEqualTo("이미 가입된 이메일입니다.");
	}

	@Test
	@DisplayName("중복 핸드폰 번호로 인한 회원가입 실패")
	void signup_ThrowsExceptionWhenPhoneNumberAlreadyExists() throws JsonProcessingException {
		String phoneNumber = "01012345678";
		String key = "phone:verified:" + phoneNumber;

		when(valueOperations.get(key)).thenReturn("{\"agreedTermsIds\":[1,2]}");
		when(objectMapper.readValue(anyString(), eq(PhoneVerifiedRedisValue.class))).thenReturn(
			phoneVerifiedRedisValue);

		when(termsRepository.findByStatusAndIsLatest(TermsStatus.ACTIVE, true)).thenReturn(List.of(
			new TermsDto(
				1L,
				TermsCode.TERMS_USE,
				"서비스 이용약관",
				TermsType.REQUIRED,
				TermsStatus.ACTIVE,
				1,
				false, // isLatest가 false
				LocalDateTime.of(2025, 3, 25, 0, 0),
				LocalDateTime.of(2026, 3, 25, 0, 0),
				1,
				null,
				null,
				List.of(
					new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
					new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
				)
			),
			new TermsDto(
				2L,
				TermsCode.TERMS_USE,
				"서비스 이용약관",
				TermsType.REQUIRED,
				TermsStatus.ACTIVE,
				1,
				false, // isLatest가 false
				LocalDateTime.of(2025, 3, 25, 0, 0),
				LocalDateTime.of(2026, 3, 25, 0, 0),
				1,
				null,
				null,
				List.of(
					new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
					new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
				)
			)
		));

		when(memberRepository.existsByPhoneNumberAndStatus(phoneNumber, MemberStatus.ACTIVE)).thenReturn(true);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			memberService.signup(signupRequest);
		});

		assertThat(exception.getMessage()).isEqualTo("이미 가입된 핸드폰 번호입니다.");
	}
}

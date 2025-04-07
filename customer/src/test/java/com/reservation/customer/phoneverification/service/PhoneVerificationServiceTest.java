package com.reservation.customer.phoneverification.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.reservation.commonapi.customer.repository.CustomerTermsRepository;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.commonmodel.terms.ClauseDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;
import com.reservation.customer.phoneverification.controller.dto.request.PhoneVerificationRequest;
import com.reservation.customer.phoneverification.controller.dto.response.PhoneVerificationResponse;

@ExtendWith(MockitoExtension.class)
public class PhoneVerificationServiceTest {

	@Mock
	private SmsSender smsSender;

	@Mock
	private CustomerTermsRepository termsRepository;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@InjectMocks
	private PhoneVerificationService phoneVerificationService;

	private PhoneVerificationRequest request;
	private VerificationCodeGeneration codeGeneration;
	private VerificationExpiresTimeGeneration expiresTimeGeneration;

	@BeforeEach
	void setUp() {
		request = new PhoneVerificationRequest("010-1234-5678", List.of(1L, 2L));
		codeGeneration = () -> "1234";
		expiresTimeGeneration = () -> LocalDateTime.now().plusMinutes(3);
	}

	@Test
	@DisplayName("인증번호 발송 성공")
	void sendVerificationNumber_Success() {
		when(termsRepository.findRequiredTerms()).thenReturn(List.of(new TermsDto(
			1L,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			1,
			true,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			null,
			null,
			List.of(
				new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
				new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
			)
		)));
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(redisTemplate.hasKey(anyString())).thenReturn(false);

		PhoneVerificationResponse response = phoneVerificationService.sendVerificationNumber(request, codeGeneration,
			expiresTimeGeneration);

		assertThat(response).isNotNull();
		verify(smsSender).send(eq(request.phoneNumber()), anyString());
		verify(valueOperations).set(anyString(), eq("1234"), any());
	}

	@Test
	@DisplayName("필수 약관 동의 체크 실패")
	void sendVerificationNumber_ThrowsExceptionWhenRequiredTermsNotAgreed() {
		when(termsRepository.findRequiredTerms()).thenReturn(List.of(new TermsDto(
			3L,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			1,
			true,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			null,
			null,
			List.of(
				new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
				new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
			)
		)));

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			phoneVerificationService.sendVerificationNumber(request, codeGeneration, expiresTimeGeneration);
		});

		assertThat(exception.getMessage()).isEqualTo("필수 약관 동의가 필요합니다");
	}

	@Test
	@DisplayName("이미 인증번호가 발송된 경우")
	void sendVerificationNumber_ThrowsExceptionWhenAlreadySent() {
		when(termsRepository.findRequiredTerms()).thenReturn(List.of(new TermsDto(
			1L,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			1,
			true,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			null,
			null,
			List.of(
				new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
				new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
			)
		)));
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(redisTemplate.hasKey(anyString())).thenReturn(true);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			phoneVerificationService.sendVerificationNumber(request, codeGeneration, expiresTimeGeneration);
		});

		assertThat(exception.getMessage()).isEqualTo("이미 인증번호가 발송되었습니다.");
	}
}

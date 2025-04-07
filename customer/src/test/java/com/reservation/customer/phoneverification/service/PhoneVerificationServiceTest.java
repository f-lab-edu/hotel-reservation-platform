package com.reservation.customer.phoneverification.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.commonapi.customer.repository.CustomerTermsRepository;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.commonmodel.terms.ClauseDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;
import com.reservation.customer.phoneverification.controller.dto.request.SendPhoneVerificationRequest;
import com.reservation.customer.phoneverification.controller.dto.request.VerifyPhoneVerificationRequest;
import com.reservation.customer.phoneverification.controller.dto.response.SendPhoneVerificationResponse;
import com.reservation.customer.phoneverification.controller.dto.response.VerifyPhoneVerificationResponse;
import com.reservation.customer.phoneverification.service.dto.PhoneVerificationRedisValue;

@ExtendWith(MockitoExtension.class)
public class PhoneVerificationServiceTest {

	@Mock
	private SmsSender smsSender;

	@Mock
	private CustomerTermsRepository termsRepository;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private PhoneVerificationService phoneVerificationService;

	private SendPhoneVerificationRequest sendRequest;
	private VerifyPhoneVerificationRequest verifyRequest;
	private PhoneVerificationRedisValue redisValue;

	@BeforeEach
	void setUp() {
		sendRequest = new SendPhoneVerificationRequest("010-1234-5678", List.of(1L, 2L));
		verifyRequest = new VerifyPhoneVerificationRequest("010-1234-5678", "1234");
		redisValue = new PhoneVerificationRedisValue("1234", Set.of(1L, 2L));
	}

	@Test
	@DisplayName("인증번호 발송 성공")
	void sendVerificationNumber_Success() throws JsonProcessingException {
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
		when(objectMapper.writeValueAsString(any())).thenReturn(
			"{\"verificationCode\":\"1234\",\"agreedTermsIds\":[1,2]}");

		SendPhoneVerificationResponse response = phoneVerificationService.sendVerificationNumber(sendRequest,
			() -> "1234", () -> LocalDateTime.now().plusMinutes(3));

		assertThat(response).isNotNull();
		verify(smsSender).send(eq("01012345678"), anyString());
		verify(valueOperations).set(anyString(), anyString(), eq(Duration.ofMinutes(3)));
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
			phoneVerificationService.sendVerificationNumber(sendRequest, () -> "1234",
				() -> LocalDateTime.now().plusMinutes(3));
		});

		assertThat(exception.getMessage()).isEqualTo("이미 인증번호가 발송되었습니다.");
	}

	@Test
	@DisplayName("인증번호 검증 성공")
	void verifyVerificationNumber_Success() throws JsonProcessingException {
		String key = "phone:verification:01012345678";
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(key)).thenReturn("{\"verificationCode\":\"1234\",\"agreedTermsIds\":[1,2]}");
		when(objectMapper.readValue(anyString(), eq(PhoneVerificationRedisValue.class))).thenReturn(redisValue);
		redisValue = new PhoneVerificationRedisValue("5555", Set.of(1L, 2L));

		VerifyPhoneVerificationResponse response = phoneVerificationService.verifyVerificationNumber(verifyRequest);

		assertThat(response).isNotNull();
		assertThat(response.agreedTermsIds()).containsExactlyInAnyOrder(1L, 2L);
		verify(redisTemplate).delete(key);
	}

	@Test
	@DisplayName("인증번호 불일치로 인한 검증 실패")
	void verifyVerificationNumber_ThrowsExceptionWhenCodeMismatch() throws JsonProcessingException {
		String key = "phone:verification:01012345678";
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(key)).thenReturn("{\"verificationCode\":\"5678\",\"agreedTermsIds\":[1,2]}");
		when(objectMapper.readValue(anyString(), eq(PhoneVerificationRedisValue.class))).thenReturn(redisValue);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			phoneVerificationService.verifyVerificationNumber(verifyRequest);
		});

		assertThat(exception.getMessage()).isEqualTo("인증번호가 일치하지 않습니다.");
	}

	@Test
	@DisplayName("인증번호 발송 내역 없음으로 인한 검증 실패")
	void verifyVerificationNumber_ThrowsExceptionWhenNoSendRecord() {
		String key = "phone:verification:01012345678";
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(key)).thenReturn(null);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			phoneVerificationService.verifyVerificationNumber(verifyRequest);
		});

		assertThat(exception.getMessage()).isEqualTo("인증번호 발송 내역이 확인되지 않습니다. 다시 시도해주세요.");
	}

	@Test
	@DisplayName("인증번호 검증 중 JSON 처리 실패")
	void verifyVerificationNumber_ThrowsExceptionWhenJsonProcessingFails() throws JsonProcessingException {
		String key = "phone:verification:01012345678";
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(key)).thenReturn("{\"verificationCode\":\"1234\",\"agreedTermsIds\":[1,2]}");
		when(objectMapper.readValue(anyString(), eq(PhoneVerificationRedisValue.class))).thenThrow(
			JsonProcessingException.class);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			phoneVerificationService.verifyVerificationNumber(verifyRequest);
		});

		assertThat(exception.getMessage()).isEqualTo("인증번호 인증에 실패했습니다. 다시 시도해주세요.");
	}
}

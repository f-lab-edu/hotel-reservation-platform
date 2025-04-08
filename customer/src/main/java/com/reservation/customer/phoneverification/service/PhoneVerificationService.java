package com.reservation.customer.phoneverification.service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.commonapi.customer.repository.CustomerTermsRepository;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.customer.phoneverification.controller.dto.request.SendPhoneVerificationRequest;
import com.reservation.customer.phoneverification.controller.dto.request.VerifyPhoneVerificationRequest;
import com.reservation.customer.phoneverification.controller.dto.response.SendPhoneVerificationResponse;
import com.reservation.customer.phoneverification.controller.dto.response.VerifyPhoneVerificationResponse;
import com.reservation.customer.phoneverification.service.dto.PhoneVerificationRedisValue;
import com.reservation.customer.phoneverification.service.dto.PhoneVerifiedRedisValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class PhoneVerificationService {
	private static final String PHONE_VERIFICATION_KEY_PREFIX = "phone:verification:";
	private static final String PHONE_VERIFIED_KEY_PREFIX = "phone:verified:";
	private static final int VERIFICATION_EXPIRES_TIME_MINUTES = 3;
	private static final int VERIFIED_EXPIRES_TIME_MINUTES = 10;

	private final SmsSender smsSender;
	private final CustomerTermsRepository termsRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public SendPhoneVerificationResponse sendVerificationNumber(SendPhoneVerificationRequest request,
		VerificationCodeGeneration codeGeneration, VerificationExpiresTimeGeneration expiresTimeGeneration) {
		// 필수 약관 동의 체크
		Set<Long> agreedTermsIds = new HashSet<>(request.agreedTermsIds());
		checkRequiredTerms(agreedTermsIds);

		// 인증번호 발송 내역 체크
		String phoneNumber = request.phoneNumber().replaceAll("-", "");
		String key = PHONE_VERIFICATION_KEY_PREFIX + phoneNumber;
		checkAlreadySendKey(key);

		// 인증번호 생성 및 저장
		String code = storeKeyAndCode(key, agreedTermsIds, codeGeneration);

		// sms 발송
		String message = String.format("인증번호는 %s입니다", code);
		smsSender.send(phoneNumber, message);

		return new SendPhoneVerificationResponse(expiresTimeGeneration.createExpiresTime());
	}

	private void checkRequiredTerms(Set<Long> agreedTermsIds) {
		List<TermsDto> findRequiredTermsList = termsRepository.findRequiredTerms();
		if (findRequiredTermsList.isEmpty()) {
			log.warn("현재 필수 약관이 존재하지 않음");
			return;
		}

		Set<Long> requiredTermsIds = findRequiredTermsList.stream()
			.map(TermsDto::id)
			.collect(Collectors.toSet());

		if (!agreedTermsIds.containsAll(requiredTermsIds)) {
			throw ErrorCode.VALIDATION_ERROR.exception("필수 약관 동의가 필요합니다");
		}
	}

	private void checkAlreadySendKey(String key) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		if (redisTemplate.hasKey(key)) {
			throw ErrorCode.CONFLICT.exception("이미 인증번호가 발송되었습니다.");
		}
	}

	private String storeKeyAndCode(String key, Set<Long> agreedTermsIds, VerificationCodeGeneration codeGeneration) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		String code = codeGeneration.createCode();
		PhoneVerificationRedisValue redisValue = new PhoneVerificationRedisValue(code, agreedTermsIds);

		try {
			String redisJson = objectMapper.writeValueAsString(redisValue); // JSON 변환
			ops.set(key, redisJson, Duration.ofMinutes(VERIFICATION_EXPIRES_TIME_MINUTES));
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw ErrorCode.CONFLICT.exception("인증번호 생성에 실패했습니다. 다시 시도해주세요.");
		}

		return code;
	}

	public VerifyPhoneVerificationResponse verifyVerificationNumber(VerifyPhoneVerificationRequest request) {
		// 인증 번호 발송 내역 확인
		String phoneNumber = request.phoneNumber().replaceAll("-", "");
		String key = PHONE_VERIFICATION_KEY_PREFIX + phoneNumber;
		PhoneVerificationRedisValue verificationRedisValue = findVerificationRedisValueBySendKey(key);

		// 인증 번호 검증
		String findVerificationCode = verificationRedisValue.verificationCode();
		if (!request.verificationCode().equals(findVerificationCode)) {
			throw ErrorCode.CONFLICT.exception("인증번호가 일치하지 않습니다.");
		}

		// 인증 완료 내역 저장
		storeVerifiedValue(phoneNumber, verificationRedisValue.agreedTermsIds());
		// 기존 key 삭제
		redisTemplate.delete(key);

		return new VerifyPhoneVerificationResponse(verificationRedisValue.agreedTermsIds());
	}

	private PhoneVerificationRedisValue findVerificationRedisValueBySendKey(String key) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		String redisJson = ops.get(key);

		if (redisJson == null || redisJson.isEmpty()) {
			throw ErrorCode.CONFLICT.exception("인증번호 발송 내역이 확인되지 않습니다. 다시 시도해주세요.");
		}

		try {
			return objectMapper.readValue(redisJson, PhoneVerificationRedisValue.class);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw ErrorCode.CONFLICT.exception("인증번호 인증에 실패했습니다. 다시 시도해주세요.");
		}
	}

	private void storeVerifiedValue(String phoneNumber, Set<Long> agreedTermsIds) {
		String key = PHONE_VERIFIED_KEY_PREFIX + phoneNumber;
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		try {
			String redisJson = objectMapper.writeValueAsString(new PhoneVerifiedRedisValue(agreedTermsIds));
			ops.set(key, redisJson, Duration.ofMinutes(VERIFIED_EXPIRES_TIME_MINUTES));
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw ErrorCode.CONFLICT.exception("인증 번호 검증에 실패했습니다. 다시 시도해주세요.");
		}
	}
}

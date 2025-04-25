package com.reservation.customer.phoneverification.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.customer.phoneverification.controller.request.VerifyPhoneVerificationRequest;
import com.reservation.customer.phoneverification.service.dto.PhoneVerificationRedisValue;
import com.reservation.customer.phoneverification.service.dto.PhoneVerifiedRedisValue;
import com.reservation.customer.terms.repository.JpaTermsRepository;
import com.reservation.domain.terms.Terms;
import com.reservation.domain.terms.enums.TermsStatus;
import com.reservation.domain.terms.enums.TermsType;
import com.reservation.support.exception.ErrorCode;

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
	private final JpaTermsRepository termsRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public LocalDateTime sendVerificationNumber(
		String phoneNumber,
		List<Long> agreedTermsIds,
		VerificationCodeGeneration codeGeneration,
		VerificationExpiresTimeGeneration expiresTimeGeneration) {

		// 필수 약관 동의 체크
		checkRequiredTerms(agreedTermsIds);

		// 인증번호 발송 내역 체크
		String transPhoneNumber = phoneNumber.replaceAll("-", "");
		String key = PHONE_VERIFICATION_KEY_PREFIX + transPhoneNumber;
		checkAlreadySendKey(key);

		// 인증번호 생성 및 저장
		String code = storeKeyAndCode(key, agreedTermsIds, codeGeneration);

		// sms 발송
		String message = String.format("인증번호는 %s 입니다", code);
		smsSender.send(phoneNumber, message);

		return expiresTimeGeneration.createExpiresTime();
	}

	private void checkRequiredTerms(List<Long> agreedTermsIds) {
		List<Terms> findRequiredTermsList = termsRepository.findByTypeAndStatus(TermsType.REQUIRED, TermsStatus.ACTIVE);
		if (findRequiredTermsList.isEmpty()) {
			log.warn("현재 필수 약관이 존재하지 않음");
			return;
		}

		Set<Long> requiredTermsIds = findRequiredTermsList.stream()
			.map(Terms::getId)
			.collect(Collectors.toSet());

		if (!new HashSet<>(agreedTermsIds).containsAll(requiredTermsIds)) {
			throw ErrorCode.VALIDATION_ERROR.exception("필수 약관 동의가 필요합니다");
		}
	}

	private void checkAlreadySendKey(String key) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		if (redisTemplate.hasKey(key)) {
			throw ErrorCode.CONFLICT.exception("이미 인증번호가 발송되었습니다.");
		}
	}

	private String storeKeyAndCode(
		String key,
		List<Long> agreedTermsIds,
		VerificationCodeGeneration codeGeneration
	) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		String code = codeGeneration.createCode();
		PhoneVerificationRedisValue redisValue = new PhoneVerificationRedisValue(code, agreedTermsIds);

		try {
			String redisJson = objectMapper.writeValueAsString(redisValue); // JSON 변환
			ops.set(key, redisJson, Duration.ofMinutes(VERIFICATION_EXPIRES_TIME_MINUTES));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw ErrorCode.CONFLICT.exception("인증번호 생성에 실패했습니다. 다시 시도해주세요.");
		}

		return code;
	}

	public List<Long> verifyVerificationNumber(VerifyPhoneVerificationRequest request) {
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

		return verificationRedisValue.agreedTermsIds();
	}

	private PhoneVerificationRedisValue findVerificationRedisValueBySendKey(String key) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		String redisJson = ops.get(key);

		if (redisJson == null || redisJson.isEmpty()) {
			throw ErrorCode.CONFLICT.exception("인증번호 발송 내역이 확인되지 않습니다. 다시 시도해주세요.");
		}

		try {
			return objectMapper.readValue(redisJson, PhoneVerificationRedisValue.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw ErrorCode.CONFLICT.exception("인증번호 인증에 실패했습니다. 다시 시도해주세요.");
		}
	}

	private void storeVerifiedValue(String phoneNumber, List<Long> agreedTermsIds) {
		String key = PHONE_VERIFIED_KEY_PREFIX + phoneNumber;
		ValueOperations<String, String> ops = redisTemplate.opsForValue();

		try {
			String redisJson = objectMapper.writeValueAsString(new PhoneVerifiedRedisValue(agreedTermsIds));
			ops.set(key, redisJson, Duration.ofMinutes(VERIFIED_EXPIRES_TIME_MINUTES));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw ErrorCode.CONFLICT.exception("인증 번호 검증에 실패했습니다. 다시 시도해주세요.");
		}
	}
}

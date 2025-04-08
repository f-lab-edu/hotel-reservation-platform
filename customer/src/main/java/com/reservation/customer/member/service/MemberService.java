package com.reservation.customer.member.service;

import static com.reservation.customer.member.service.mapper.MemberDtoMapper.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.commonapi.customer.repository.CustomerMemberRepository;
import com.reservation.commonapi.customer.repository.CustomerTermsRepository;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.member.MemberDto;
import com.reservation.commonmodel.member.MemberStatus;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;
import com.reservation.customer.member.controller.dto.request.SignupRequest;
import com.reservation.customer.phoneverification.service.dto.PhoneVerifiedRedisValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberService {
	private static final String PHONE_VERIFIED_KEY_PREFIX = "phone:verified:";

	private final CustomerMemberRepository memberRepository;
	private final CustomerTermsRepository termsRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;
	private final PasswordEncoder passwordEncoder;

	public void signup(SignupRequest request) {
		// verifiedKey: 핸드폰 인증
		String phoneNumber = request.phoneNumber().replaceAll("-", "");
		String key = PHONE_VERIFIED_KEY_PREFIX + phoneNumber;
		List<Long> acceptedTermsIds = checkVerifiedKey(key);

		// 약관 동의 체크
		List<TermsDto> checkTermsList = checkTermsList(acceptedTermsIds);

		// 중복 가입 체크
		checkDuplicateMember(request.email(), phoneNumber);

		// Customer 저장
		// 패스워드 저장 시 BCrypt
		String encryptedPassword = passwordEncoder.encode(request.password());
		MemberDto memberDto = fromSignupRequestAndPasswordAndTermsDtoList(request, encryptedPassword, checkTermsList);
		memberRepository.save(memberDto);

		// verifiedKey: 핸드폰 인증 내역 삭제
		redisTemplate.delete(key);
	}

	private List<Long> checkVerifiedKey(String key) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		String redisJson = ops.get(key);

		if (redisJson == null || redisJson.isEmpty()) {
			throw ErrorCode.CONFLICT.exception("핸드폰 인증이 확인되지 않습니다. 다시 시도해주세요.");
		}

		try {
			PhoneVerifiedRedisValue phoneVerifiedRedisValue = objectMapper.readValue(redisJson,
				PhoneVerifiedRedisValue.class);
			return phoneVerifiedRedisValue.agreedTermsIds().stream().toList();
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw ErrorCode.CONFLICT.exception("핸드폰 인증 확인에 실패했습니다. 다시 시도해주세요.");
		}
	}

	private List<TermsDto> checkTermsList(List<Long> acceptedTermsIds) {
		List<TermsDto> checkTermsList = new ArrayList<>();

		List<TermsDto> latestTermsList = termsRepository.findByStatusAndIsLatest(TermsStatus.ACTIVE, true);
		if (latestTermsList.isEmpty()) {
			throw ErrorCode.INTERNAL_SERVER_ERROR.exception("약관 정보가 없습니다.");
		}
		
		for (TermsDto latestTerms : latestTermsList) {
			if (latestTerms.type() == TermsType.REQUIRED && !acceptedTermsIds.contains(latestTerms.id())) {
				throw ErrorCode.CONFLICT.exception("필수 약관 정보에 동의하지 않았습니다.");
			}
			if (acceptedTermsIds.contains(latestTerms.id())) {
				checkTermsList.add(latestTerms);
			}
		}

		if (checkTermsList.size() != acceptedTermsIds.size()) {
			throw ErrorCode.CONFLICT.exception("약관 정보가 올바르지 않습니다.");
		}

		return checkTermsList;
	}

	private void checkDuplicateMember(String email, String phoneNumber) {
		if (memberRepository.existsByEmailAndStatus(email, MemberStatus.ACTIVE)) {
			throw ErrorCode.CONFLICT.exception("이미 가입된 이메일입니다.");
		}
		if (memberRepository.existsByPhoneNumberAndStatus(phoneNumber, MemberStatus.ACTIVE)) {
			throw ErrorCode.CONFLICT.exception("이미 가입된 핸드폰 번호입니다.");
		}
	}
}

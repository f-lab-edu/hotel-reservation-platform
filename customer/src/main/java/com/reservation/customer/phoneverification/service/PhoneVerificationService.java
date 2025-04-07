package com.reservation.customer.phoneverification.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.reservation.commonapi.customer.repository.CustomerTermsRepository;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.customer.phoneverification.controller.dto.request.PhoneVerificationRequest;
import com.reservation.customer.phoneverification.controller.dto.response.PhoneVerificationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class PhoneVerificationService {
	private final SmsSender smsSender;
	private final CustomerTermsRepository termsRepository;

	public PhoneVerificationResponse sendVerificationNumber(PhoneVerificationRequest request,
		PhoneVerificationCodeGeneration codeGeneration) {
		checkRequiredTerms(new HashSet<>(request.agreedTermsIds()));

		String verificationCode = codeGeneration.createCode();

		String message = String.format("인증번호는 %s입니다", verificationCode);
		smsSender.send(request.phoneNumber(), message);

		return null;
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
}

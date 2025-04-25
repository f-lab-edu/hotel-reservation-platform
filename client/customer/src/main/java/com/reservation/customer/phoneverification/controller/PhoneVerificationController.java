package com.reservation.customer.phoneverification.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.customer.phoneverification.controller.request.SendPhoneVerificationRequest;
import com.reservation.customer.phoneverification.controller.request.VerifyPhoneVerificationRequest;
import com.reservation.customer.phoneverification.controller.response.SendPhoneVerificationResponse;
import com.reservation.customer.phoneverification.controller.response.VerifyPhoneVerificationResponse;
import com.reservation.customer.phoneverification.infra.PhoneVerificationCodeGenerator;
import com.reservation.customer.phoneverification.infra.PhoneVerificationExpiresTimeGenerator;
import com.reservation.customer.phoneverification.service.PhoneVerificationService;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/no-auth/phone-verification")  //❗JWT auth 제외
@Tag(name = "폰 인증 API", description = "고객용 폰 인증 API 입니다.")
@RequiredArgsConstructor
public class PhoneVerificationController {
	private final PhoneVerificationService phoneVerificationService;

	@PostMapping("/send")
	@Operation(summary = "폰 인증 번호 전송", description = "랜덤한 인증 번호를 생성하여 고객의 폰으로 전송합니다.")
	public ApiResponse<SendPhoneVerificationResponse> sendVerificationNumber(
		@RequestBody SendPhoneVerificationRequest request
	) {
		LocalDateTime expiresTime = phoneVerificationService.sendVerificationNumber(
			request.phoneNumber(),
			request.agreedTermsIds(),
			PhoneVerificationCodeGenerator::createCode,
			PhoneVerificationExpiresTimeGenerator::createExpiresTime);

		return ApiResponse.ok(new SendPhoneVerificationResponse(expiresTime));
	}

	@PostMapping("/verify")
	@Operation(summary = "폰 인증 번호 검증", description = "전송한 인증 번호를 검증합니다.")
	public ApiResponse<VerifyPhoneVerificationResponse> verifyVerificationNumber(
		@RequestBody VerifyPhoneVerificationRequest request
	) {
		List<Long> agreedTermsIds = phoneVerificationService.verifyVerificationNumber(request);

		return ApiResponse.ok(new VerifyPhoneVerificationResponse(agreedTermsIds));
	}

}

package com.reservation.customer.phoneverification.controller;

import static com.reservation.common.response.ApiResponse.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.common.response.ApiResponse;
import com.reservation.customer.phoneverification.controller.dto.request.PhoneVerificationRequest;
import com.reservation.customer.phoneverification.controller.dto.response.PhoneVerificationResponse;
import com.reservation.customer.phoneverification.infra.PhoneVerificationCodeGenerator;
import com.reservation.customer.phoneverification.service.PhoneVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/phone-verification")
@Tag(name = "폰 인증 API", description = "고객용 폰 인증 API입니다.")
@RequiredArgsConstructor
public class PhoneVerificationController {
	private final PhoneVerificationService phoneVerificationService;

	@PostMapping("/send")
	@Operation(summary = "폰 인증 번호 발송")
	public ApiResponse<PhoneVerificationResponse> sendVerificationNumber(
		@RequestBody PhoneVerificationRequest request) {
		PhoneVerificationResponse response = phoneVerificationService.sendVerificationNumber(request,
			PhoneVerificationCodeGenerator::createCode);
		
		return ok(response);
	}

}

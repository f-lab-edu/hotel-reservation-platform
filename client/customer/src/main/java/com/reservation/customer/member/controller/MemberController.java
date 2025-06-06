package com.reservation.customer.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.customer.member.controller.request.SignupRequest;
import com.reservation.customer.member.service.MemberService;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "일반 회원 관리 API", description = "일반 회원 관리 API입니다.")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	@PostMapping("/no-auth/member/signup")  //❗JWT auth 제외
	@Operation(summary = "회원 가입", description = "일반 고객 회원가입 API 입니다.")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
		memberService.signup(request.email(), request.password(), request.phoneNumber());
		
		return ApiResponse.noContent();
	}
}

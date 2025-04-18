package com.reservation.customer.member.controller;

import static com.reservation.common.response.ApiResponse.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.common.response.ApiResponse;
import com.reservation.customer.member.controller.dto.request.SignupRequest;
import com.reservation.customer.member.service.MemberService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/member")
@Tag(name = "일반 회원 관리 API", description = "일반 회원 관리 API입니다.")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
		memberService.signup(request);
		return noContent();
	}
}

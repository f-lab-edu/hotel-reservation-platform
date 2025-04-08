package com.reservation.customer.member.controller;

import static com.reservation.common.response.ApiResponse.*;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.common.response.ApiResponse;
import com.reservation.commonauth.auth.JwtTokenProvider;
import com.reservation.commonmodel.member.MemberDto;
import com.reservation.customer.member.controller.dto.request.LoginRequest;
import com.reservation.customer.member.controller.dto.request.SignupRequest;
import com.reservation.customer.member.service.MemberService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("member")
@Tag(name = "일반 회원 관리 API", description = "일반 회원 관리 API입니다.")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
		memberService.signup(request);
		return noContent();
	}

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
		Long memberId = memberService.login(request);

		String accessToken = jwtTokenProvider.generateToken(memberId, "ROLE_CUSTOMER");
		String refreshToken = jwtTokenProvider.generateRefreshToken(memberId, "ROLE_CUSTOMER");
		ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofDays(7))
			.build();

		return ResponseEntity.noContent()
			.header("Authorization", "Bearer " + accessToken)
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.build();
	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public ApiResponse<MemberDto> getMe(@AuthenticationPrincipal Long memberId) {
		return ok(memberService.findMe(memberId));
	}
}

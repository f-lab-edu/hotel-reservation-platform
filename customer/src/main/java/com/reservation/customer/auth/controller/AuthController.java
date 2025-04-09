package com.reservation.customer.auth.controller;

import static com.reservation.common.response.ApiResponse.*;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.common.response.ApiResponse;
import com.reservation.commonmodel.member.MemberDto;
import com.reservation.customer.auth.annotation.LoginMember;
import com.reservation.customer.auth.controller.dto.request.LoginRequest;
import com.reservation.customer.auth.service.AuthService;
import com.reservation.customer.auth.service.dto.LoginDto;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping()
@Tag(name = "인증 API", description = "일반 회원 인증 API입니다.")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
		LoginDto loginDto = authService.login(request);

		ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginDto.refreshToken())
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofDays(7))
			.build();

		return ResponseEntity.noContent()
			.header("Authorization", "Bearer " + loginDto.accessToken())
			.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
			.build();
	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('ROLE_CUSTOMER')")
	public ApiResponse<MemberDto> getMe(@LoginMember Long memberId) {
		return ok(authService.findMe(memberId));
	}
}

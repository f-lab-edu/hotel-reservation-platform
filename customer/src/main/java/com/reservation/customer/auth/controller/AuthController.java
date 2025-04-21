package com.reservation.customer.auth.controller;

import static com.reservation.common.response.ApiResponse.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.common.response.ApiResponse;
import com.reservation.commonauth.auth.annotation.LoginUserId;
import com.reservation.commonauth.auth.login.LogoutService;
import com.reservation.commonauth.auth.login.RefreshService;
import com.reservation.commonmodel.auth.Role;
import com.reservation.commonmodel.auth.login.SocialLoginProvider;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.commonmodel.member.MemberDto;
import com.reservation.customer.auth.controller.dto.request.LoginRequest;
import com.reservation.customer.auth.service.AuthService;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping()
@Tag(name = "인증 API", description = "일반 회원 인증 API입니다.")
@RequiredArgsConstructor
public class AuthController {
	public static final String PRE_AUTH_ROLE_CUSTOMER = "hasRole('ROLE_CUSTOMER')";

	private final AuthService authService;
	private final RefreshService refreshService;
	private final LogoutService logoutService;

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/oauth2/callback/google")
	public ResponseEntity<Void> socialLoginByGoogle(@RequestParam String code) {
		SocialLoginProvider provider = SocialLoginProvider.GOOGLE;
		try {
			return authService.login(provider, code);
		} catch (BusinessException e) {
			return provider.fallbackRedirect(e.getMessage());
		} catch (Exception e) {
			return provider.fallbackUnknown();
		}
	}

	@GetMapping("/oauth2/callback/github")
	public ResponseEntity<Void> socialLoginByGithub(@RequestParam String code) {
		SocialLoginProvider provider = SocialLoginProvider.GITHUB;
		try {
			return authService.login(provider, code);
		} catch (BusinessException e) {
			return provider.fallbackRedirect(e.getMessage());
		} catch (Exception e) {
			return provider.fallbackUnknown();
		}
	}

	@GetMapping("/me")
	@PreAuthorize(PRE_AUTH_ROLE_CUSTOMER)
	public ApiResponse<MemberDto> getMe(@Schema(hidden = true) @LoginUserId Long memberId) {
		return ok(authService.findMe(memberId));
	}

	@GetMapping("/token/refresh")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize(PRE_AUTH_ROLE_CUSTOMER)
	public ResponseEntity<Void> tokenReissue(@Schema(hidden = true) @LoginUserId Long memberId) {
		return refreshService.tokenReissue(memberId, Role.CUSTOMER);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize(PRE_AUTH_ROLE_CUSTOMER)
	public ResponseEntity<Void> logout(@Schema(hidden = true) @LoginUserId Long memberId) {
		return logoutService.logout(memberId, Role.CUSTOMER);
	}
}

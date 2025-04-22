package com.reservation.customer.auth.controller;

import static com.reservation.common.response.ApiResponse.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "인증 API", description = "일반 회원 인증 API입니다.")
@RequiredArgsConstructor
public class AuthController {
	public static final String PRE_AUTH_ROLE_CUSTOMER = "hasRole('ROLE_CUSTOMER')";

	private final AuthService authService;
	private final RefreshService refreshService;
	private final LogoutService logoutService;

	@PostMapping("no-auth/login") //❗JWT auth 제외
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "일반 로그인", description = "일반 고객 로그인 API 입니다, 로그인 성공 시 JWT 토큰을 발급합니다")
	public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("no-auth/oauth2/callback/google") //❗JWT auth 제외
	@Operation(summary = "구글 소셜 로그인", description = "일반 고객 구글 로그인 연동 API 입니다, code 값은 구글에서 발급받은 코드입니다")
	public ResponseEntity<Void> socialLoginByGoogle(@RequestParam("code") String code) {
		SocialLoginProvider provider = SocialLoginProvider.GOOGLE;
		try {
			return authService.login(provider, code);
		}
		//✅의도된 예외인 경우
		catch (BusinessException e) {
			return provider.fallbackRedirect(e.getMessage());
		}
		//❌예상치 못한 예외인 경우
		catch (Exception e) {
			return provider.fallbackUnknown();
		}
	}

	@GetMapping("no-auth/oauth2/callback/github") //❗JWT auth 제외
	@Operation(summary = "깃헙 소셜 로그인", description = "일반 고객 깃헙 로그인 연동 API 입니다, code 값은 깃헙에서 발급받은 코드입니다")
	public ResponseEntity<Void> socialLoginByGithub(@RequestParam("code") String code) {
		SocialLoginProvider provider = SocialLoginProvider.GITHUB;
		try {
			return authService.login(provider, code);
		}
		//✅의도된 예외인 경우
		catch (BusinessException e) {
			return provider.fallbackRedirect(e.getMessage());
		}
		//❌예상치 못한 예외인 경우
		catch (Exception e) {
			return provider.fallbackUnknown();
		}
	}

	@GetMapping("/no-auth/refresh") //❗JWT auth 제외
	@Operation(summary = "토큰 재발급", description = "일반 고객 AT 재발급 API 입니다, 기존 토큰은 만료됩니다")
	public ResponseEntity<Void> tokenReissue(
		@LoginUserId(includeExpired = true) Long memberId) { // AccessToken 만료되어도 재발급 가능
		return refreshService.tokenReissue(memberId, Role.CUSTOMER);
	}

	@PostMapping("/auth/logout")
	@PreAuthorize(PRE_AUTH_ROLE_CUSTOMER)//✅일반 고객만 접근 가능
	@Operation(summary = "로그아웃", description = "일반 고객 로그아웃 API 입니다")
	public ResponseEntity<Void> logout(@LoginUserId Long memberId) {
		return logoutService.logout(memberId, Role.CUSTOMER);
	}

	@GetMapping("/auth/me")
	@PreAuthorize(PRE_AUTH_ROLE_CUSTOMER) //✅일반 고객만 접근 가능
	@Operation(summary = "ME", description = "일반 고객 정보 확인 API 입니다")
	public ApiResponse<MemberDto> getMe(@LoginUserId Long memberId) {
		MemberDto member = authService.findMe(memberId);
		return ok(member);
	}
}

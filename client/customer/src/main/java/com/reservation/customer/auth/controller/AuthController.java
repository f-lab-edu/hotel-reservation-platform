package com.reservation.customer.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.auth.annotation.LoginUserId;
import com.reservation.auth.login.LogoutService;
import com.reservation.auth.login.RefreshService;
import com.reservation.auth.login.dto.AccessTokenHeader;
import com.reservation.auth.login.dto.LoginSettingToken;
import com.reservation.auth.login.dto.OauthSettingToken;
import com.reservation.auth.login.dto.RefreshTokenCookie;
import com.reservation.customer.auth.controller.request.LoginRequest;
import com.reservation.customer.auth.service.AuthService;
import com.reservation.domain.member.Member;
import com.reservation.support.enums.Role;
import com.reservation.support.enums.SocialLoginProvider;
import com.reservation.support.exception.BusinessException;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "일반 회원 인증 AP I입니다.")
public class AuthController {
	public static final String PRE_AUTH_ROLE_CUSTOMER = "hasRole('ROLE_CUSTOMER')";

	private final AuthService authService;
	private final RefreshService refreshService;
	private final LogoutService logoutService;

	@PostMapping("no-auth/login") //❗JWT auth 제외
	@Operation(summary = "일반 로그인", description = "일반 고객 로그인 API 입니다, 로그인 성공 시 JWT 토큰을 발급합니다")
	@CrossOrigin(origins = "http://localhost:63342", allowCredentials = "true")
	public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
		LoginSettingToken loginSettingToken = authService.login(request.email(), request.password());

		RefreshTokenCookie refreshTokenCookie = loginSettingToken.refreshTokenCookie();
		ResponseCookie responseCookie = ResponseCookie.from(refreshTokenCookie.name(), refreshTokenCookie.value())
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(refreshTokenCookie.refreshDuration())
			.build();

		AccessTokenHeader accessTokenHeader = loginSettingToken.accessTokenHeader();

		return ResponseEntity.noContent()
			.header(accessTokenHeader.name(), accessTokenHeader.value())
			.header(HttpHeaders.SET_COOKIE, responseCookie.toString())
			.header("Access-Control-Expose-Headers", accessTokenHeader.name())
			.build();
	}

	@GetMapping("no-auth/oauth2/callback/google") //❗JWT auth 제외
	@Operation(summary = "구글 소셜 로그인", description = "일반 고객 구글 로그인 연동 API 입니다, code 값은 구글에서 발급받은 코드입니다")
	public ResponseEntity<Void> socialLoginByGoogle(@RequestParam String code) {
		return getOauthResponseEntity(code, SocialLoginProvider.GOOGLE);
	}

	@GetMapping("no-auth/oauth2/callback/github") //❗JWT auth 제외
	@Operation(summary = "깃헙 소셜 로그인", description = "일반 고객 깃헙 로그인 연동 API 입니다, code 값은 깃헙에서 발급받은 코드입니다")
	public ResponseEntity<Void> socialLoginByGithub(@RequestParam String code) {
		return getOauthResponseEntity(code, SocialLoginProvider.GITHUB);
	}

	private ResponseEntity<Void> getOauthResponseEntity(String code, SocialLoginProvider provider) {
		try {
			OauthSettingToken oauthSettingToken = authService.login(provider, code);
			//👋🏻회원 가입 필요한 경우
			if (!oauthSettingToken.isRegistered()) {
				return ResponseEntity
					.status(HttpStatus.FOUND)
					.header(HttpHeaders.LOCATION, provider.getSocialSignupUrl(oauthSettingToken.email()))
					.build();
			}
			LoginSettingToken loginSettingToken = oauthSettingToken.loginSettingToken();
			RefreshTokenCookie refreshTokenCookie = loginSettingToken.refreshTokenCookie();
			ResponseCookie responseCookie = ResponseCookie.from(refreshTokenCookie.name(), refreshTokenCookie.value())
				.httpOnly(true)
				.secure(true)
				.path("/")
				.maxAge(refreshTokenCookie.refreshDuration())
				.build();

			AccessTokenHeader accessTokenHeader = loginSettingToken.accessTokenHeader();

			return ResponseEntity.status(HttpStatus.FOUND)
				.header(HttpHeaders.LOCATION, provider.getRedirectUrl())
				.header(accessTokenHeader.name(), accessTokenHeader.value())
				.header(HttpHeaders.SET_COOKIE, responseCookie.toString())
				.build();
		}
		//✅의도된 예외인 경우
		catch (BusinessException e) {
			return ResponseEntity
				.status(HttpStatus.FOUND)
				.header(HttpHeaders.LOCATION, provider.fallbackRedirectUrl(e.getMessage()))
				.build();
		}
		//❌예상치 못한 예외인 경우
		catch (Exception e) {
			return ResponseEntity
				.status(HttpStatus.FOUND)
				.header(HttpHeaders.LOCATION, provider.fallbackUnknown())
				.build();
		}
	}

	@GetMapping("/no-auth/refresh") //❗JWT auth 제외 -> @LoginUserId 토큰 검증
	@Operation(summary = "토큰 재발급", description = "일반 고객 AT 재발급 API 입니다, 기존 토큰은 만료됩니다")
	public ResponseEntity<Void> tokenReissue(@LoginUserId(includeExpired = true) Long memberId) {

		AccessTokenHeader accessTokenHeader = refreshService.tokenReissue(memberId, Role.CUSTOMER);

		return ResponseEntity.noContent()
			.header(accessTokenHeader.name(), accessTokenHeader.value())
			.build();
	}

	@PostMapping("/auth/logout")
	@PreAuthorize(PRE_AUTH_ROLE_CUSTOMER) //✅일반 고객만 접근 가능
	@Operation(summary = "로그아웃", description = "일반 고객 로그아웃 API 입니다")
	public ResponseEntity<Void> logout(@LoginUserId long memberId) {
		String deleteRefreshTokenCookieKey = logoutService.logout(memberId, Role.CUSTOMER);

		ResponseCookie deleteCookie = ResponseCookie.from(deleteRefreshTokenCookieKey, "")
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(0)
			.build();

		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
			.build();
	}

	@GetMapping("/auth/me")
	@PreAuthorize(PRE_AUTH_ROLE_CUSTOMER) //✅일반 고객만 접근 가능
	@Operation(summary = "ME", description = "일반 고객 정보 확인 API 입니다")
	public ApiResponse<Member> getMe(@LoginUserId long memberId) {
		Member findMember = authService.getMe(memberId);

		return ApiResponse.ok(findMember);
	}
}

package com.reservation.host.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.auth.annotation.LoginUserId;
import com.reservation.auth.login.LogoutService;
import com.reservation.auth.login.RefreshService;
import com.reservation.auth.login.Role;
import com.reservation.auth.login.dto.AccessTokenHeader;
import com.reservation.auth.login.dto.LoginSettingToken;
import com.reservation.auth.login.dto.RefreshTokenCookie;
import com.reservation.domain.host.Host;
import com.reservation.host.auth.controller.request.LoginRequest;
import com.reservation.host.auth.service.AuthService;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "인증 API", description = "숙박 업체 인증 API 입니다.")
@RequiredArgsConstructor
public class AuthController {
	public static final String PRE_AUTH_ROLE_HOST = "hasRole('ROLE_HOST')";

	private final AuthService authService;
	private final RefreshService refreshService;
	private final LogoutService logoutService;

	@PostMapping("/no-auth/login") //❗JWT auth 제외
	@Operation(summary = "일반 로그인", description = "숙박 업체 로그인 API 입니다, 로그인 성공 시 JWT 토큰을 발급합니다")
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
			.build();
	}

	@GetMapping("/no-auth/refresh") //❗JWT auth 제외 -> @LoginUserId 토큰 검증
	@Operation(summary = "토큰 재발급", description = "숙박 업체 AT 재발급 API 입니다, 기존 토큰은 만료됩니다")
	public ResponseEntity<Void> tokenReissue(@LoginUserId(includeExpired = true) long hostId) {
		AccessTokenHeader accessTokenHeader = refreshService.tokenReissue(hostId, Role.HOST);

		return ResponseEntity.noContent()
			.header(accessTokenHeader.name(), accessTokenHeader.value())
			.build();
	}

	@PostMapping("/auth/logout")
	@PreAuthorize(PRE_AUTH_ROLE_HOST)//✅숙박 업체만 접근 가능
	@Operation(summary = "로그아웃", description = "숙박 업체 로그아웃 API 입니다")
	public ResponseEntity<Void> logout(@LoginUserId long hostId) {
		String deleteRefreshTokenCookieKey = logoutService.logout(hostId, Role.HOST);

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
	@PreAuthorize(PRE_AUTH_ROLE_HOST)//✅숙박 업체만 접근 가능
	@Operation(summary = "ME", description = "숙박 업체 정보 확인 API 입니다")
	public ApiResponse<Host> getMe(@LoginUserId long hostId) {
		return ApiResponse.ok(authService.findMe(hostId));
	}
}

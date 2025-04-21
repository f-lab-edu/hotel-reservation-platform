package com.reservation.host.auth.controller;

import static com.reservation.common.response.ApiResponse.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.common.response.ApiResponse;
import com.reservation.commonauth.auth.annotation.LoginUserId;
import com.reservation.commonauth.auth.login.LogoutService;
import com.reservation.commonauth.auth.login.RefreshService;
import com.reservation.commonmodel.auth.Role;
import com.reservation.commonmodel.host.HostDto;
import com.reservation.host.auth.controller.dto.request.LoginRequest;
import com.reservation.host.auth.service.AuthService;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping()
@Tag(name = "인증 API", description = "숙박 업체 인증 API입니다.")
@RequiredArgsConstructor
public class AuthController {
	public static final String PRE_AUTH_ROLE_HOST = "hasRole('ROLE_HOST')";

	private final AuthService authService;
	private final RefreshService refreshService;
	private final LogoutService logoutService;

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/token/refresh")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> tokenReissue(
		@Schema(hidden = true) @LoginUserId Long hostId) {
		return refreshService.tokenReissue(hostId, Role.HOST);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize(PRE_AUTH_ROLE_HOST)
	public ResponseEntity<Void> logout(@Schema(hidden = true) @LoginUserId Long hostId) {
		return logoutService.logout(hostId, Role.HOST);
	}

	@GetMapping("/me")
	@PreAuthorize(PRE_AUTH_ROLE_HOST)
	public ApiResponse<HostDto> getMe(@Schema(hidden = true) @LoginUserId Long hostId) {
		return ok(authService.findMe(hostId));
	}
}

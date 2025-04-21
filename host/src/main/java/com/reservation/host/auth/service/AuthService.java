package com.reservation.host.auth.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reservation.commonapi.host.repository.HostModuleRepository;
import com.reservation.commonauth.auth.login.LoginService;
import com.reservation.commonmodel.auth.Role;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.host.HostDto;
import com.reservation.commonmodel.host.HostStatus;
import com.reservation.host.auth.controller.dto.request.LoginRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {
	private final HostModuleRepository hostRepository;
	private final PasswordEncoder passwordEncoder;
	private final LoginService loginService;

	public ResponseEntity<Void> login(@Valid LoginRequest request) {
		HostDto hostDto = hostRepository.findOneByEmailAndStatusIsNot(request.email(), HostStatus.WITHDRAWN)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("이메일 정보가 존재하지 않습니다."));
		if (hostDto.status() == HostStatus.SUSPENDED) {
			throw ErrorCode.NOT_FOUND.exception("정지 계정 입니다. 고객 센터로 연락 바랍니다.");
		}
		if (!passwordEncoder.matches(request.password(), hostDto.password())) {
			throw ErrorCode.NOT_FOUND.exception("로그인 정보가 일치하지 않습니다.");
		}

		return loginService.login(hostDto.id(), Role.HOST, "https://hotel-reservation-frontend.com/login/success");
	}

	public HostDto findMe(Long hostId) {
		return hostRepository.findById(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("호스트 정보를 찾을 수 없습니다."));
	}
}

package com.reservation.host.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reservation.auth.login.LoginService;
import com.reservation.auth.login.dto.LoginSettingToken;
import com.reservation.domain.host.Host;
import com.reservation.domain.host.enums.HostStatus;
import com.reservation.host.host.repository.JpaHostRepository;
import com.reservation.support.enums.Role;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {
	private final JpaHostRepository jpaHostRepository;
	private final PasswordEncoder passwordEncoder;
	private final LoginService loginService;

	public LoginSettingToken login(String email, String password) {
		Host findHost = jpaHostRepository.findOneByEmailAndStatusIsNot(email, HostStatus.WITHDRAWN)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("이메일 정보가 존재하지 않습니다."));

		if (findHost.getStatus() == HostStatus.SUSPENDED) {
			throw ErrorCode.NOT_FOUND.exception("정지 계정 입니다. 고객 센터로 연락 바랍니다.");
		}
		if (!passwordEncoder.matches(password, findHost.getPassword())) {
			throw ErrorCode.NOT_FOUND.exception("로그인 정보가 일치하지 않습니다.");
		}

		return loginService.login(findHost.getId(), Role.HOST);
	}

	public Host findMe(long hostId) {
		return jpaHostRepository.findById(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("호스트 정보를 찾을 수 없습니다."));
	}
}

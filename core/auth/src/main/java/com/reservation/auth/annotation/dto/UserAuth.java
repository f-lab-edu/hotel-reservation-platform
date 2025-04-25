package com.reservation.auth.annotation.dto;

import com.reservation.auth.login.Role;
import com.reservation.support.exception.ErrorCode;

public record UserAuth(Long userId, Role role) {
	public UserAuth {
		if (userId == null || userId <= 0) {
			throw ErrorCode.UNAUTHORIZED.exception("유효하지 않은 사용자 ID입니다.");
		}
		if (role == null) {
			throw ErrorCode.UNAUTHORIZED.exception("유효하지 않은 권한입니다.");
		}
	}
}

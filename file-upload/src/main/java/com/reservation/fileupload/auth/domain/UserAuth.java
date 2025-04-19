package com.reservation.fileupload.auth.domain;

import com.reservation.commonauth.auth.domain.Role;
import com.reservation.commonmodel.exception.ErrorCode;

import lombok.Getter;

@Getter
public class UserAuth {
	private final Long userId;
	private final Role role;

	public UserAuth(Long userId, Role role) {
		if (userId == null || userId <= 0) {
			throw ErrorCode.UNAUTHORIZED.exception("유효하지 않은 사용자 ID입니다.");
		}
		if (role == null) {
			throw ErrorCode.UNAUTHORIZED.exception("유효하지 않은 권한입니다.");
		}
		this.userId = userId;
		this.role = role;
	}
}

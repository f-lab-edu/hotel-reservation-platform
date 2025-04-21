package com.reservation.fileupload.auth.dto;

import com.reservation.commonmodel.auth.Role;
import com.reservation.commonmodel.exception.ErrorCode;

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

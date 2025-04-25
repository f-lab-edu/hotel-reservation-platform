package com.reservation.auth.login;

import com.reservation.support.enums.Role;

public interface BlacklistService {
	void setBlacklistToken(Long userId, Role role);

	boolean checkBlacklistToken(String token);
}

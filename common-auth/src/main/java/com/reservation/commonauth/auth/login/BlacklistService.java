package com.reservation.commonauth.auth.login;

import com.reservation.commonmodel.auth.Role;

public interface BlacklistService {
	void setBlacklistToken(Long userId, Role role);

	boolean checkBlacklistToken(String token);
}

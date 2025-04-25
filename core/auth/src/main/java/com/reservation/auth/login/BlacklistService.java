package com.reservation.auth.login;

public interface BlacklistService {
	void setBlacklistToken(Long userId, Role role);

	boolean checkBlacklistToken(String token);
}

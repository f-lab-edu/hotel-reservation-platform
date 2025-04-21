package com.reservation.commonauth.auth.login.social;

public interface OAuthClient {
	OAuthUserInfo getUserInfo(String authCode);
}

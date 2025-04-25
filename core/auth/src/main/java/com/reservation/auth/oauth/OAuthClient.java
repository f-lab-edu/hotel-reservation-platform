package com.reservation.auth.oauth;

public interface OAuthClient {
	OAuthUserInfo getUserInfo(String authCode);
}

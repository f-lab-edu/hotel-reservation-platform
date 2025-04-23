package com.reservation.commonapi.auth.oauth;

public interface OAuthClient {
	OAuthUserInfo getUserInfo(String authCode);
}

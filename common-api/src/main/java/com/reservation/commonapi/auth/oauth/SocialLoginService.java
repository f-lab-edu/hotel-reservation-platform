package com.reservation.commonapi.auth.oauth;

import com.reservation.commonmodel.auth.login.SocialLoginProvider;

public interface SocialLoginService {
	OAuthUserInfo authenticate(SocialLoginProvider provider, String code);
}

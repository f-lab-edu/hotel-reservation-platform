package com.reservation.auth.oauth;

import com.reservation.support.enums.SocialLoginProvider;

public interface SocialLoginService {
	OAuthUserInfo authenticate(SocialLoginProvider provider, String code);
}

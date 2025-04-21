package com.reservation.commonauth.auth.login.social;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.reservation.commonmodel.auth.login.SocialLoginProvider;
import com.reservation.commonmodel.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SocialLoginService {

	private final Map<String, OAuthClient> clientMap;

	public OAuthUserInfo authenticate(SocialLoginProvider provider, String code) {
		OAuthClient client = clientMap.get(provider.name());
		if (client == null) {
			throw ErrorCode.UNAUTHORIZED.exception("Not Supported Provider");
		}
		return client.getUserInfo(code);
	}
}

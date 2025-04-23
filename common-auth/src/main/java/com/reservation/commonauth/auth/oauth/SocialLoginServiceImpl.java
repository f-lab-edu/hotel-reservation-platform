package com.reservation.commonauth.auth.oauth;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.reservation.commonapi.auth.oauth.OAuthClient;
import com.reservation.commonapi.auth.oauth.OAuthUserInfo;
import com.reservation.commonapi.auth.oauth.SocialLoginService;
import com.reservation.commonmodel.auth.login.SocialLoginProvider;
import com.reservation.commonmodel.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SocialLoginServiceImpl implements SocialLoginService {

	private final Map<String, OAuthClient> clientMap;

	public OAuthUserInfo authenticate(SocialLoginProvider provider, String code) {
		OAuthClient client = clientMap.get(provider.name());
		if (client == null) {
			throw ErrorCode.UNAUTHORIZED.exception("Not Supported Provider");
		}
		return client.getUserInfo(code);
	}
}

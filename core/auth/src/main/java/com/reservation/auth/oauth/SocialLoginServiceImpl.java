package com.reservation.auth.oauth;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.reservation.support.enums.SocialLoginProvider;
import com.reservation.support.exception.ErrorCode;

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

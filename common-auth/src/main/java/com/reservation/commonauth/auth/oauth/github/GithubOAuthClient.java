package com.reservation.commonauth.auth.oauth.github;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.reservation.commonapi.auth.oauth.OAuthClient;
import com.reservation.commonapi.auth.oauth.OAuthUserInfo;
import com.reservation.commonmodel.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component("GITHUB")
@Qualifier("GITHUB")
@RequiredArgsConstructor
public class GithubOAuthClient implements OAuthClient {
	@Value("${oauth2.github.client-id}")
	private String clientId;

	@Value("${oauth2.github.client-secret}")
	private String clientSecret;

	@Value("${oauth2.github.redirect_uri}")
	private String redirectUri;

	@Value("${oauth2.github.grant_type}")
	private String grantType;

	private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
	private static final String USER_INFO_URL = "https://api.github.com/user";

	private final RestTemplate restTemplate;

	@Override
	public OAuthUserInfo getUserInfo(String authCode) {
		GithubTokenResponse token = githubTokenResponse(authCode);
		if (token == null) {
			throw ErrorCode.UNAUTHORIZED.exception("Failed to get access token");
		}
		return githubUserInfo(token.getAccess_token());
	}

	private GithubTokenResponse githubTokenResponse(String authCode) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("code", authCode);
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("redirect_uri", redirectUri);
		params.add("grant_type", grantType);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

		ResponseEntity<GithubTokenResponse> response = restTemplate.postForEntity(
			TOKEN_URL,
			request,
			GithubTokenResponse.class
		);

		return response.getBody();
	}

	private GithubUserInfo githubUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<?> request = new HttpEntity<>(headers);
		ResponseEntity<GithubUserInfo> response = restTemplate.exchange(
			USER_INFO_URL,
			HttpMethod.GET,
			request,
			GithubUserInfo.class
		);

		return response.getBody();
	}
}

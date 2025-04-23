package com.reservation.commonauth.auth.login.social.google;

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

import com.reservation.commonauth.auth.login.social.OAuthClient;
import com.reservation.commonauth.auth.login.social.OAuthUserInfo;
import com.reservation.commonmodel.exception.ErrorCode;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component("GOOGLE")
@Qualifier("GOOGLE")
@Log4j2
@RequiredArgsConstructor
public class GoogleOAuthClient implements OAuthClient {
	@Value("${oauth2.google.client-id}")
	private String clientId;

	@Value("${oauth2.google.client-secret}")
	private String clientSecret;

	@Value("${oauth2.google.redirect_uri}")
	private String redirectUri;

	@Value("${oauth2.google.grant_type}")
	private String grantType;

	private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
	private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

	private final RestTemplate restTemplate;

	@Override
	@Retry(name = "googleOAuth", fallbackMethod = "googleFallback")
	@CircuitBreaker(name = "googleOAuth", fallbackMethod = "googleFallback")
	public OAuthUserInfo getUserInfo(String authCode) {
		GoogleTokenResponse token = googleTokenResponse(authCode);
		if (token == null) {
			throw ErrorCode.UNAUTHORIZED.exception("Failed to get access token");
		}
		return googleUserInfo(token.getAccess_token());
	}

	private GoogleTokenResponse googleTokenResponse(String authCode) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("code", authCode);
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("redirect_uri", redirectUri);
		params.add("grant_type", grantType);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

		ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
			TOKEN_URL,
			request,
			GoogleTokenResponse.class
		);

		return response.getBody();
	}

	private GoogleUserInfo googleUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<?> request = new HttpEntity<>(headers);
		ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
			USER_INFO_URL,
			HttpMethod.GET,
			request,
			GoogleUserInfo.class
		);

		return response.getBody();
	}

	public OAuthUserInfo googleFallback(String code, Throwable t) {
		log.warn("Google 로그인 실패 fallback 진입: {}", t.getMessage());
		throw ErrorCode.UNAUTHORIZED.exception("google-login-fallback");
	}
}

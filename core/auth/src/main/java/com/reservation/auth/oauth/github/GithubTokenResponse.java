package com.reservation.auth.oauth.github;

import lombok.Data;

@Data
public class GithubTokenResponse {
	private String access_token;
	private String token_type;
	private String scope;
}

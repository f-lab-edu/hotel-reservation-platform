package com.reservation.auth.oauth.google;

import lombok.Data;

@Data
public class GoogleTokenResponse {
	private String access_token;
	private String expires_in;
	private String token_type;
	private String scope;
	private String id_token;
}

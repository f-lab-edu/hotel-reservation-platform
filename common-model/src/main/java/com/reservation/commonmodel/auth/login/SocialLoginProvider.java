package com.reservation.commonmodel.auth.login;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public enum SocialLoginProvider {
	GOOGLE("https://hotel-reservation-frontend.com/social-fail"),
	GITHUB("https://hotel-reservation-frontend.com/social-fail");

	private final String fallbackBaseUrl;

	SocialLoginProvider(String fallbackBaseUrl) {
		this.fallbackBaseUrl = fallbackBaseUrl;
	}

	public ResponseEntity<Void> fallbackRedirect(String reason) {
		String queryString = reason != null ? "?reason=" + reason : "";
		String fallbackRedirectUrl = fallbackBaseUrl + queryString;
		return ResponseEntity
			.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, fallbackRedirectUrl)
			.build();
	}

	public ResponseEntity<Void> fallbackUnknown() {
		return fallbackRedirect("UNKNOWN");
	}
}

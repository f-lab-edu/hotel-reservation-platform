package com.reservation.support.enums;

public enum SocialLoginProvider {
	GOOGLE, GITHUB;

	private static final String FALLBACK_BASE_URL = "https://hotel-reservation-frontend.com/social-fail";
	private static final String REDIRECT_URL = "https://hotel-reservation-frontend.com/login/success";
	private static final String SOCIAL_SIGNUP_URL = "https://hotel-reservation-frontend.com/signup?email=";

	public String getRedirectUrl() {
		return REDIRECT_URL;
	}

	public String getSocialSignupUrl(String email) {
		return SOCIAL_SIGNUP_URL + email;
	}

	public String fallbackRedirectUrl(String reason) {
		String queryString = reason != null ? "?reason=" + reason : "";
		return FALLBACK_BASE_URL + queryString;
	}

	public String fallbackUnknown() {
		return fallbackRedirectUrl("UNKNOWN");
	}
}

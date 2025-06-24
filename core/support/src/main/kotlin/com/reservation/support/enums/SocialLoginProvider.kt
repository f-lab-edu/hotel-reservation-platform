package com.reservation.support.enums

enum class SocialLoginProvider {
    GOOGLE, GITHUB;

    val redirectUrl: String
        get() = REDIRECT_URL

    fun getSocialSignupUrl(email: String): String {
        return SOCIAL_SIGNUP_URL + email
    }

    fun fallbackRedirectUrl(reason: String?): String {
        val queryString = if (reason != null) "?reason=$reason" else ""
        return FALLBACK_BASE_URL + queryString
    }

    fun fallbackUnknown(): String {
        return fallbackRedirectUrl("UNKNOWN")
    }

    companion object {
        private const val FALLBACK_BASE_URL: String = "https://hotel-reservation-frontend.com/social-fail"
        private const val REDIRECT_URL: String = "https://hotel-reservation-frontend.com/login/success"
        private const val SOCIAL_SIGNUP_URL: String = "https://hotel-reservation-frontend.com/signup?email="
    }
}

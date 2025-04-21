package com.reservation.commonauth.auth.login.social.google;

import com.reservation.commonauth.auth.login.social.OAuthUserInfo;

public record GoogleUserInfo(String email) implements OAuthUserInfo {
}

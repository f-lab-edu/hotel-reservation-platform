package com.reservation.commonauth.auth.oauth.google;

import com.reservation.commonapi.auth.oauth.OAuthUserInfo;

public record GoogleUserInfo(String email) implements OAuthUserInfo {
}

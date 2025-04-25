package com.reservation.auth.oauth.google;

import com.reservation.auth.oauth.OAuthUserInfo;

public record GoogleUserInfo(String email) implements OAuthUserInfo {
}

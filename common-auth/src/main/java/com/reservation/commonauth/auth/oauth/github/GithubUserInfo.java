package com.reservation.commonauth.auth.oauth.github;

import com.reservation.commonapi.auth.oauth.OAuthUserInfo;

public record GithubUserInfo(String email) implements OAuthUserInfo {
}

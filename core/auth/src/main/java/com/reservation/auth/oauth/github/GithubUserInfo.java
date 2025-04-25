package com.reservation.auth.oauth.github;

import com.reservation.auth.oauth.OAuthUserInfo;

public record GithubUserInfo(String email) implements OAuthUserInfo {
}

package com.reservation.commonauth.auth.login.social.github;

import com.reservation.commonauth.auth.login.social.OAuthUserInfo;

public record GithubUserInfo(String email) implements OAuthUserInfo {
}

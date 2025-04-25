package com.reservation.auth.login.dto;

public record LoginSettingToken(AccessTokenHeader accessTokenHeader, RefreshTokenCookie refreshTokenCookie) {
}

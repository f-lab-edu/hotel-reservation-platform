package com.reservation.commonmodel.auth.login;

public record LoginSettingToken(AccessTokenHeader accessTokenHeader, RefreshTokenCookie refreshTokenCookie) {
}

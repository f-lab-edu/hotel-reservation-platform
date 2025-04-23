package com.reservation.commonmodel.auth.login;

public record OauthSettingToken(LoginSettingToken loginSettingToken, Boolean isRegistered, String email) {
}

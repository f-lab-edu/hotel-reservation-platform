package com.reservation.auth.login.dto;

public record OauthSettingToken(LoginSettingToken loginSettingToken, Boolean isRegistered, String email) {
}

package com.reservation.commonmodel.auth.login;

import java.time.Duration;

public record RefreshTokenCookie(String name, String value, Duration refreshDuration) {
}

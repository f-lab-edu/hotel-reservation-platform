package com.reservation.auth.login.dto;

import java.time.Duration;

public record RefreshTokenCookie(String name, String value, Duration refreshDuration) {
}

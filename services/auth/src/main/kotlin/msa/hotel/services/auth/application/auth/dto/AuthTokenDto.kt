package msa.hotel.services.auth.application.auth.dto

import java.time.Instant

data class AuthTokenDto(
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenIssuedAt: Instant,
    val refreshTokenExpiration: Instant,
)

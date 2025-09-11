package com.msa.identityservice.auth.token.dto

import java.time.Duration


data class LoginAuthToken(
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenDuration: Duration,
)

package com.msa.identityservice.auth.token.dto

import java.time.Instant

data class RefreshTokenInfo(
    val accessTokenJti: String,
    val token: String,
    val loginAt: Instant,
    val lastActivityAt: Instant,
    val expiresAt: Instant,
)

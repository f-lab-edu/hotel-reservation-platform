package com.msa.identityservice.auth.token.dto

import java.time.Duration


data class RefreshTokenCookie(
    val name: String,
    val value: String,
    val refreshDuration: Duration
)

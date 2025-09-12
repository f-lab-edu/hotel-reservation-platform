package com.msa.gatewayservice.auth.token.dto

import com.msa.identityservice.auth.token.enums.Role
import java.util.*


data class TokenAuthInfo(
    val jti: String,
    val userId: Long,
    val role: Role,
    val email: String,
    val deviceId: String,
    val expiresAt: Date,
)

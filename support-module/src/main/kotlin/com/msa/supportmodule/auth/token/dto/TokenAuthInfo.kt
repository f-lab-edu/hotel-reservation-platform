package com.msa.supportmodule.auth.token.dto

import com.msa.supportmodule.auth.token.enums.Role
import java.util.*


data class TokenAuthInfo(
    val jti: String,
    val userId: Long,
    val role: Role,
    val email: String,
    val deviceId: String,
    val expiresAt: Date,
)

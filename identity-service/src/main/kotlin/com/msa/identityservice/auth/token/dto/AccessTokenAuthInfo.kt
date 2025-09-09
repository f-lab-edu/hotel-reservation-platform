package com.msa.identityservice.auth.token.dto

import com.msa.identityservice.auth.token.enums.Role
import java.util.*


data class AccessTokenAuthInfo(
    val jti: String,
    val userId: Long,
    val email: String,
    val role: Role,
    val expiresAt: Date,
)

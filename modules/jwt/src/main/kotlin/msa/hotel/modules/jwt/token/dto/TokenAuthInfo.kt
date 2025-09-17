package msa.hotel.modules.jwt.token.dto

import msa.hotel.modules.jwt.enums.Role
import java.util.*

data class TokenAuthInfo(
    val jti: String,
    val userId: Long,
    val role: Role,
    val deviceId: String,
    val expiration: Date,
)

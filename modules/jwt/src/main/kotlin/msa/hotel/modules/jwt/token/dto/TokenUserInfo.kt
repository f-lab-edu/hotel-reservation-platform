package msa.hotel.modules.jwt.token.dto

data class TokenUserInfo(
    val role: String,
    val userId: ULong,
    val deviceId: String,
)

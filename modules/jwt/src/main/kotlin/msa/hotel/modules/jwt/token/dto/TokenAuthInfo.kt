package msa.hotel.modules.jwt.token.dto

import java.util.Date

data class TokenAuthInfo(
    val jti: String,
    val tokenUserInfo: TokenUserInfo,
    val expiration: Date,
)

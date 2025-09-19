package msa.hotel.modules.jwt.token.dto

data class Token(
    val value: String,
    val jti: String,
)

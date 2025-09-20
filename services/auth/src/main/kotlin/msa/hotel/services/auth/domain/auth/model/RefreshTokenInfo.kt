package msa.hotel.services.auth.domain.auth.model

import msa.hotel.modules.web.exception.ErrorCode
import java.time.Instant

data class RefreshTokenInfo(
    val accessTokenJti: String,
    val token: String,
    val loginAt: Instant,
    val lastActivityAt: Instant,
    val expiresAt: Instant,
) {
    init {
        if (accessTokenJti.isBlank()) {
            throw ErrorCode.CONFLICT.exception("access token jti is blank")
        }
        if (token.isBlank()) {
            throw ErrorCode.CONFLICT.exception("refresh token is blank")
        }
        if (lastActivityAt.isBefore(loginAt)) {
            throw ErrorCode.CONFLICT.exception("last activity at is before login at")
        }
        if (expiresAt.isBefore(loginAt)) {
            throw ErrorCode.CONFLICT.exception("refresh token expires at is before login at")
        }
    }
}

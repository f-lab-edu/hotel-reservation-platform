package msa.hotel.services.auth.domain.auth.port

import msa.hotel.modules.jwt.token.dto.TokenUserInfo
import msa.hotel.services.auth.domain.auth.model.RefreshTokenInfo
import java.time.Instant

interface AuthTokenRepository {
    fun findRefreshTokenInfo(userInfo: TokenUserInfo): RefreshTokenInfo?

    fun saveAuthTokens(
        userInfo: TokenUserInfo,
        refreshTokenInfo: RefreshTokenInfo,
        issuedAt: Instant,
        accessTokenExpiration: Instant,
        refreshTokenExpiration: Instant,
        maxLoginClient: UInt,
        pastActiveJti: String? = null,
    )

    fun existActiveJti(jti: String): Boolean

    fun findRefreshTokenInfo(
        userInfo: TokenUserInfo,
        logoutDeviceId: String,
    ): RefreshTokenInfo?

    fun deleteAuthTokenByLogout(
        userInfo: TokenUserInfo,
        logoutActiveJti: String,
        logoutDeviceId: String,
    )

    fun findAllRefreshTokenInfo(userInfo: TokenUserInfo): List<RefreshTokenInfo>

    fun deleteAuthTokenByLogoutAll(userInfo: TokenUserInfo)
}

package msa.hotel.services.auth.infrastructure.persistence.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import msa.hotel.modules.jwt.token.dto.TokenUserInfo
import msa.hotel.services.auth.domain.auth.model.RefreshTokenInfo
import msa.hotel.services.auth.domain.auth.port.AuthTokenRepository
import msa.hotel.services.auth.infrastructure.persistence.redis.key.AuthTokenKey.makeActiveJtiKey
import msa.hotel.services.auth.infrastructure.persistence.redis.key.AuthTokenKey.makeRefreshTokenKey
import msa.hotel.services.auth.infrastructure.persistence.redis.key.AuthTokenKey.makeSessionKey
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Repository
class AuthTokenRepositoryImpl(
    private val rt: RedisTemplate<String, String>,
    private val om: ObjectMapper,
    private val loginScript: RedisScript<Long>,
    private val logoutScript: RedisScript<Long>,
    private val logoutAllScript: RedisScript<Long>,
) : AuthTokenRepository {
    override fun findRefreshTokenInfo(userInfo: TokenUserInfo): RefreshTokenInfo? {
        val key = makeRefreshTokenKey(userInfo.role, userInfo.userId)
        val refreshTokenInfoString = rt.opsForHash<String, String>().get(key, userInfo.deviceId) ?: return null

        return om.readValue(refreshTokenInfoString, RefreshTokenInfo::class.java)
    }

    override fun findRefreshTokenInfo(
        userInfo: TokenUserInfo,
        logoutDeviceId: String,
    ): RefreshTokenInfo? {
        val key = makeRefreshTokenKey(userInfo.role, userInfo.userId)
        val refreshTokenInfoString = rt.opsForHash<String, String>().get(key, logoutDeviceId) ?: return null

        return om.readValue(refreshTokenInfoString, RefreshTokenInfo::class.java)
    }

    override fun saveAuthTokens(
        userInfo: TokenUserInfo,
        refreshTokenInfo: RefreshTokenInfo,
        issuedAt: Instant,
        accessTokenExpiration: Instant,
        refreshTokenExpiration: Instant,
        maxLoginClient: UInt,
        pastActiveJti: String?,
    ) {
        // Lua 스크립트 실행을 위한 파라미터 준비 (최대 동시 접속 가능 기기 5개로 세션 정보 관리)
        val activeJtiKey = makeActiveJtiKey(refreshTokenInfo.accessTokenJti)
        val sessionAgesKey = makeSessionKey(role = userInfo.role, userId = userInfo.userId)
        val refreshTokensKey = makeRefreshTokenKey(role = userInfo.role, userId = userInfo.userId)

        val refreshTokenExpiresAt = refreshTokenExpiration.epochSecond.toString()
        val accessTokenTtl = Duration.between(issuedAt, accessTokenExpiration).seconds
        val refreshTokenTtl = Duration.between(issuedAt, refreshTokenExpiration).seconds
        val sessionInfoJson = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(refreshTokenInfo)

        val pastActiveJtiKey: String? = if (pastActiveJti != null) makeActiveJtiKey(pastActiveJti) else null

        rt.execute(
            loginScript,
            listOf(sessionAgesKey, refreshTokensKey, activeJtiKey, pastActiveJtiKey),
            maxLoginClient.toString(),
            userInfo.deviceId,
            refreshTokenExpiresAt,
            sessionInfoJson,
            userInfo.userId.toString(),
            accessTokenTtl.toString(),
            refreshTokenTtl.toString(),
        )
    }

    override fun existActiveJti(jti: String): Boolean {
        val activeJtiKey = makeActiveJtiKey(jti)

        rt.opsForValue().get(activeJtiKey) ?: return false

        return true
    }

    override fun deleteAuthTokenByLogout(
        userInfo: TokenUserInfo,
        logoutActiveJti: String,
        logoutDeviceId: String,
    ) {
        val refreshTokenKey = makeRefreshTokenKey(role = userInfo.role, userId = userInfo.userId)
        val sessionAgesKey = makeSessionKey(role = userInfo.role, userId = userInfo.userId)
        val activeJtiKey = makeActiveJtiKey(logoutActiveJti)

        // --- 원자적 스크립트 실행 ---
        rt.execute(
            logoutScript,
            listOf(refreshTokenKey, sessionAgesKey, activeJtiKey),
            logoutDeviceId,
        )
    }

    override fun findAllRefreshTokenInfo(userInfo: TokenUserInfo): List<RefreshTokenInfo> {
        val refreshTokensKey = makeRefreshTokenKey(role = userInfo.role, userId = userInfo.userId)
        val refreshTokenEntries = rt.opsForHash<String, String>().entries(refreshTokensKey)

        return refreshTokenEntries.values.map { om.readValue(it, RefreshTokenInfo::class.java) }
    }

    override fun deleteAuthTokenByLogoutAll(userInfo: TokenUserInfo) {
        // Lua 스크립트 실행을 위한 파라미터 준비 (해당 유저의 모든 AccessToken, RefreshToken 무력화)
        val refreshTokenKey = makeRefreshTokenKey(role = userInfo.role, userId = userInfo.userId)
        val sessionAgesKey = makeSessionKey(role = userInfo.role, userId = userInfo.userId)

        // --- 원자적 스크립트 실행 ---
        rt.execute(
            logoutAllScript,
            listOf(refreshTokenKey, sessionAgesKey),
            UUID.randomUUID().toString(),
        )
    }
}

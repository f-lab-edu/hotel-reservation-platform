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

@Repository
class AuthTokenRepositoryImpl(
    private val rt: RedisTemplate<String, String>,
    private val om: ObjectMapper,
    private val loginScript: RedisScript<Long>,
) : AuthTokenRepository {
    override fun findRefreshTokenInfo(userInfo: TokenUserInfo): RefreshTokenInfo? {
        val key = makeRefreshTokenKey(userInfo.role, userInfo.userId)
        val refreshTokenInfoString = rt.opsForHash<String, String>().get(key, userInfo.deviceId) ?: return null

        return om.readValue(refreshTokenInfoString, RefreshTokenInfo::class.java)
    }

    override fun saveAuthTokens(
        userInfo: TokenUserInfo,
        accessTokenJti: String,
        refreshTokenInfo: RefreshTokenInfo,
        issuedAt: Instant,
        accessTokenExpiration: Instant,
        refreshTokenExpiration: Instant,
        maxLoginClient: UInt,
        pastActiveJti: String?,
    ) {
        // Lua 스크립트 실행을 위한 파라미터 준비 (최대 동시 접속 가능 기기 5개로 세션 정보 관리)
        val activeJtiKey = makeActiveJtiKey(accessTokenJti)
        val sessionAgesKey = makeSessionKey(role = userInfo.role, userId = userInfo.userId)
        val refreshTokensKey = makeRefreshTokenKey(role = userInfo.role, userId = userInfo.userId)

        val refreshTokenExpiresAt = refreshTokenExpiration.epochSecond.toString()
        val accessTokenTtl = Duration.between(issuedAt, accessTokenExpiration).seconds
        val refreshTokenTtl = Duration.between(issuedAt, refreshTokenExpiration).seconds
        val sessionInfoJson = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(refreshTokenInfo)

        rt.execute(
            loginScript,
            listOf(sessionAgesKey, refreshTokensKey, activeJtiKey, pastActiveJti),
            maxLoginClient.toString(),
            userInfo.deviceId,
            refreshTokenExpiresAt,
            sessionInfoJson,
            userInfo.userId.toString(),
            accessTokenTtl.toString(),
            refreshTokenTtl.toString(),
        )
    }
}

package com.msa.identityservice.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.msa.identityservice.auth.service.dto.LoginRequestInfo
import com.msa.identityservice.auth.token.JwtTokenProvider
import com.msa.identityservice.auth.token.RequestContext
import com.msa.identityservice.auth.token.dto.AccessTokenHeader
import com.msa.identityservice.auth.token.dto.LoginAuthToken
import com.msa.identityservice.auth.token.dto.RefreshTokenCookie
import com.msa.identityservice.auth.token.dto.RefreshTokenInfo
import com.msa.identityservice.auth.token.enums.Role
import com.msa.identityservice.config.properties.JwtProperties
import com.msa.identityservice.exception.BusinessErrorCode
import com.msa.identityservice.jooq.enums.MemberStatus
import com.msa.identityservice.member.repository.MemberRepository
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*


@Service
class AuthService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val requestContext: RequestContext,
    private val jwtProperties: JwtProperties,
    private val redisTemplate: RedisTemplate<String, String>,
    private val atomicLoginScript: RedisScript<Long>,
    private val objectMapper: ObjectMapper
) {

    fun login(loginRequestInfo: LoginRequestInfo): LoginAuthToken {
        // 1. 사용자 인증
        val loginUserId = validLoginUser(
            role = loginRequestInfo.role,
            email = loginRequestInfo.email,
            password = loginRequestInfo.password
        )

        // 2. 인증 토큰 생성 및 저장
        return generateAuthToken(
            userId = loginUserId,
            email = loginRequestInfo.email,
            role = loginRequestInfo.role,
            deviceId = loginRequestInfo.deviceId
        )
    }

    private fun validLoginUser(
        role: Role,
        email: String,
        password: String,
    ): Long {
        var userId = 0L

        if (role == Role.MEMBER) {
            val member = memberRepository.findByStatusAndEmail(MemberStatus.ACTIVE, email)
                ?: throw BusinessErrorCode.BAD_REQUEST.exception("존재하지 않는 이메일입니다.")

            if (!passwordEncoder.matches(password, member.password)) {
                throw BusinessErrorCode.UNAUTHORIZED.exception("비밀번호가 일치하지 않습니다.")
            }
            userId = member.id
        }

        return userId
    }

    private fun generateAuthToken(
        userId: Long,
        email: String,
        role: Role,
        deviceId: String,
        loginAt: Instant = Instant.now()
    ): LoginAuthToken {
        // AccessToken 생성
        val now = Instant.now()
        val accessTokenExpiration = now.plus(Duration.ofHours(jwtProperties.accessTokenExpirationHours))
        val accessToken = jwtTokenProvider.generateJsonWebToken(
            issuedAt = Date.from(now),
            expiration = Date.from(accessTokenExpiration),
            userId = userId,
            email = email,
            role = role,
            deviceId = deviceId
        )

        // RefreshToken 생성
        val refreshTokenExpiration = now.plus(Duration.ofHours(jwtProperties.refreshTokenExpirationHours))
        val refreshToken = jwtTokenProvider.generateJsonWebToken(
            issuedAt = Date.from(now),
            expiration = Date.from(refreshTokenExpiration),
            userId = userId,
            email = email,
            role = role,
            deviceId = deviceId
        )

        // --- Lua 스크립트 실행을 위한 파라미터 준비 ---
        val activeTokenKey = getActiveKey(
            role = role,
            userId = userId,
            deviceId = deviceId
        )
        val accessTokenTtl = Duration.between(now, accessTokenExpiration).seconds
        val sessionAgesKey = getSessionKey(role, userId)
        val refreshTokensKey = getRefreshTokenKey(role, userId)
        val refreshTokenExpiresAt = refreshTokenExpiration.epochSecond.toString()
        val refreshTokenInfo = RefreshTokenInfo(
            token = refreshToken,
            loginAt = loginAt,
            lastActivityAt = now,
            expiresAt = refreshTokenExpiration,
        )
        val sessionInfoJson = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(refreshTokenInfo)
        val deleteActiveKeyPrefix = "$ACTIVE_KEY_PREFIX:${role.name.lowercase()}:$userId"

        // --- 원자적 스크립트 실행 ---
        redisTemplate.execute(
            atomicLoginScript,
            listOf(sessionAgesKey, refreshTokensKey, activeTokenKey),
            jwtProperties.maxDeviceCount.toString(),
            deviceId,
            refreshTokenExpiresAt,
            sessionInfoJson,
            userId.toString(),
            accessTokenTtl.toString(),
            deleteActiveKeyPrefix
        )

        // For AccessToken Header Setting
        val accessTokenHeaderValue = "${RequestContext.AUTH_HEADER_PREFIX}$accessToken"
        val accessTokenHeader = AccessTokenHeader(
            name = RequestContext.AUTH_HEADER_NAME,
            value = accessTokenHeaderValue
        )

        // For RefreshToken Cookie Setting
        val refreshTokenCookie = RefreshTokenCookie(
            name = RequestContext.REFRESH_COOKIE_NAME,
            value = refreshToken,
            refreshDuration = Duration.between(Instant.now(), refreshTokenExpiration)
        )

        return LoginAuthToken(
            accessTokenHeader = accessTokenHeader,
            refreshTokenCookie = refreshTokenCookie
        )
    }

    fun tokenReissue(): LoginAuthToken {
        // 1. AccessToken 유효성 검사
        val accessToken = requestContext.getAccessToken()
        val tokenAuthInfo = try {
            jwtTokenProvider.extractAuthInfo(accessToken)
        } catch (expiredJwtException: ExpiredJwtException) {
            // AccessToken 만료된 경우에도 토큰 재발급을 위해 예외에서 정보 추출
            jwtTokenProvider.extractAuthInfo(expiredJwtException)
        }

        // 2. RefreshToken 유효성 검사
        val refreshToken = requestContext.getRefreshToken()
        val refreshTokenInfo = checkStoredRefreshTokenThrow(
            refreshToken = refreshToken,
            role = tokenAuthInfo.role,
            userId = tokenAuthInfo.userId,
            deviceId = tokenAuthInfo.deviceId
        )

        // 3. 인증 토큰 재발급
        return generateAuthToken(
            userId = tokenAuthInfo.userId,
            email = tokenAuthInfo.email,
            role = tokenAuthInfo.role,
            deviceId = tokenAuthInfo.deviceId,
            loginAt = refreshTokenInfo.loginAt
        )
    }

    private fun checkStoredRefreshTokenThrow(
        refreshToken: String,
        role: Role,
        userId: Long,
        deviceId: String
    ): RefreshTokenInfo {
        val key = getRefreshTokenKey(role, userId)
        val refreshTokenInfoString = redisTemplate.opsForHash<String, String>().get(key, deviceId)
            ?: throw BusinessErrorCode.UNAUTHORIZED.exception("인증 세션 정보가 존재하지 않습니다.")
        val refreshTokenInfo = objectMapper.readValue(refreshTokenInfoString, RefreshTokenInfo::class.java)

        if (refreshTokenInfo.token != refreshToken) {
            throw BusinessErrorCode.UNAUTHORIZED.exception("인증 정보가 올바르지 않습니다.")
        }

        return refreshTokenInfo
    }

    private fun getRefreshTokenKey(role: Role, userId: Long) = "$REFRESH_TOKEN_PREFIX:${role.name.lowercase()}:$userId"

    private fun getActiveKey(role: Role, userId: Long, deviceId: String) =
        "$ACTIVE_KEY_PREFIX:${role.name.lowercase()}:$userId:$deviceId"

    private fun getSessionKey(role: Role, userId: Long) = "$SESSIONS_AGES_PREFIX:${role.name.lowercase()}:${userId}"

    companion object {
        const val SESSIONS_AGES_PREFIX = "sessions_ages"
        const val ACTIVE_KEY_PREFIX = "active"
        const val REFRESH_TOKEN_PREFIX = "refresh_token"
    }

}

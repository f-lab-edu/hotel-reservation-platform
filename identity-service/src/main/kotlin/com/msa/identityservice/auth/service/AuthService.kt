package com.msa.identityservice.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.msa.identityservice.auth.consts.AuthConstants.getActiveJtiKey
import com.msa.identityservice.auth.consts.AuthConstants.getRefreshTokenKey
import com.msa.identityservice.auth.consts.AuthConstants.getSessionKey
import com.msa.identityservice.auth.service.dto.LoginRequestInfo
import com.msa.identityservice.auth.service.dto.SessionInfo
import com.msa.identityservice.auth.token.JwtTokenProvider
import com.msa.identityservice.auth.token.RequestContext
import com.msa.identityservice.auth.token.dto.LoginAuthToken
import com.msa.identityservice.auth.token.dto.RefreshTokenInfo
import com.msa.identityservice.auth.token.dto.TokenAuthInfo
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
import java.time.ZoneId
import java.util.*


@Service
class AuthService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val requestContext: RequestContext,
    private val jwtProperties: JwtProperties,
    private val redisTemplate: RedisTemplate<String, String>,
    private val loginScript: RedisScript<Long>,
    private val logoutScript: RedisScript<Long>,
    private val logoutAllScript: RedisScript<Long>,
    private val objectMapper: ObjectMapper
) : ICheckActiveJtiService {

    fun login(loginRequestInfo: LoginRequestInfo): LoginAuthToken {
        // 1. 사용자 인증
        val loginUserId = validLoginUser(
            role = loginRequestInfo.role,
            email = loginRequestInfo.email,
            password = loginRequestInfo.password
        )

        // 2. 같은 기기 중복 로그인 체크
        val pastActiveJtiKey = checkDuplicateLogin(
            role = loginRequestInfo.role,
            userId = loginUserId,
            deviceId = loginRequestInfo.deviceId
        )

        // 3. 인증 토큰 생성 및 저장
        return generateAuthToken(
            userId = loginUserId,
            email = loginRequestInfo.email,
            role = loginRequestInfo.role,
            deviceId = loginRequestInfo.deviceId,
            pastActiveJtiKey = pastActiveJtiKey
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

    private fun checkDuplicateLogin(
        role: Role,
        userId: Long,
        deviceId: String
    ): String? {
        val refreshTokenKey = getRefreshTokenKey(role = role, userId = userId)
        val refreshTokenInfoString =
            redisTemplate.opsForHash<String, String>().get(refreshTokenKey, deviceId) ?: return null

        val refreshTokenInfo = objectMapper.readValue(refreshTokenInfoString, RefreshTokenInfo::class.java)

        return getActiveJtiKey(refreshTokenInfo.accessTokenJti)
    }

    private fun generateAuthToken(
        userId: Long,
        email: String,
        role: Role,
        deviceId: String,
        loginAt: Instant = Instant.now(),
        pastActiveJtiKey: String? = null
    ): LoginAuthToken {
        // 1. AccessToken 생성
        val now = Instant.now()
        val accessTokenJti = UUID.randomUUID().toString()
        val accessTokenExpiration = now.plus(Duration.ofHours(jwtProperties.accessTokenExpirationHours))
        val accessToken = jwtTokenProvider.generateJsonWebToken(
            jti = accessTokenJti,
            issuedAt = Date.from(now),
            expiration = Date.from(accessTokenExpiration),
            userId = userId,
            email = email,
            role = role,
            deviceId = deviceId
        )

        // 2. RefreshToken 생성
        val refreshTokenJti = UUID.randomUUID().toString()
        val refreshTokenExpiration = now.plus(Duration.ofHours(jwtProperties.refreshTokenExpirationHours))
        val refreshToken = jwtTokenProvider.generateJsonWebToken(
            jti = refreshTokenJti,
            issuedAt = Date.from(now),
            expiration = Date.from(refreshTokenExpiration),
            userId = userId,
            email = email,
            role = role,
            deviceId = deviceId
        )

        // 3. Lua 스크립트 실행을 위한 파라미터 준비 (최대 동시 접속 가능 기기 5개로 세션 정보 관리)
        val activeJtiKey = getActiveJtiKey(accessTokenJti)
        val sessionAgesKey = getSessionKey(role = role, userId = userId)
        val refreshTokensKey = getRefreshTokenKey(role = role, userId = userId)
        val refreshTokenExpiresAt = refreshTokenExpiration.epochSecond.toString()
        val refreshTokenInfo = RefreshTokenInfo(
            accessTokenJti = accessTokenJti,
            token = refreshToken,
            loginAt = loginAt,
            lastActivityAt = now,
            expiresAt = refreshTokenExpiration,
        )
        val sessionInfoJson = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(refreshTokenInfo)
        val accessTokenTtl = Duration.between(now, accessTokenExpiration).seconds
        val refreshTokenTtl = Duration.between(now, refreshTokenExpiration).seconds

        // --- 원자적 스크립트 실행 ---
        redisTemplate.execute(
            loginScript,
            listOf(sessionAgesKey, refreshTokensKey, activeJtiKey, pastActiveJtiKey),
            jwtProperties.maxDeviceCount.toString(),
            deviceId,
            refreshTokenExpiresAt,
            sessionInfoJson,
            userId.toString(),
            accessTokenTtl.toString(),
            refreshTokenTtl.toString(),
        )

        return LoginAuthToken(
            accessToken = accessToken,
            refreshToken = refreshToken,
            refreshTokenDuration = Duration.between(Instant.now(), refreshTokenExpiration),
        )
    }

    fun tokenReissue(): LoginAuthToken {
        // 1. AccessToken 유효성 검사
        var isExpired = false
        val accessToken = requestContext.getAccessToken()
        val claims = try {
            jwtTokenProvider.getClaims(accessToken)
        } catch (expiredJwtException: ExpiredJwtException) {
            // AccessToken 만료된 경우에도 토큰 재발급을 위해 예외에서 정보 추출
            isExpired = true
            expiredJwtException.claims
        }

        // 2. 만료된 AccessToken 토큰이 아닌 경우, Active JTI 검증
        if (!isExpired) {
            checkActiveJti()
        }

        val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(claims)


        // 3. RefreshToken 유효성 검사
        val refreshToken = requestContext.getRefreshToken()
        val refreshTokenInfo = checkStoredRefreshTokenThrow(
            refreshToken = refreshToken,
            role = tokenAuthInfo.role,
            userId = tokenAuthInfo.userId,
            deviceId = tokenAuthInfo.deviceId
        )

        // 4. 인증 토큰 재발급
        return generateAuthToken(
            userId = tokenAuthInfo.userId,
            email = tokenAuthInfo.email,
            role = tokenAuthInfo.role,
            deviceId = tokenAuthInfo.deviceId,
            loginAt = refreshTokenInfo.loginAt,
            pastActiveJtiKey = getActiveJtiKey(tokenAuthInfo.jti)
        )
    }

    private fun checkStoredRefreshTokenThrow(
        refreshToken: String,
        role: Role,
        userId: Long,
        deviceId: String
    ): RefreshTokenInfo {
        val key = getRefreshTokenKey(role = role, userId = userId)
        val refreshTokenInfoString = redisTemplate.opsForHash<String, String>().get(key, deviceId)
            ?: throw BusinessErrorCode.UNAUTHORIZED.exception("인증 세션 정보가 존재하지 않습니다.")
        val refreshTokenInfo = objectMapper.readValue(refreshTokenInfoString, RefreshTokenInfo::class.java)

        if (refreshTokenInfo.token != refreshToken) {
            throw BusinessErrorCode.UNAUTHORIZED.exception("인증 정보가 올바르지 않습니다.")
        }

        return refreshTokenInfo
    }

    fun getSessions(): List<SessionInfo> {
        // 1. AccessToken 유효성 검사
        val tokenAuthInfo = checkActiveJti()

        // 2. 리프레쉬 토큰 세션 Hash 조회 -> 현재 접속 중인 로그인 기기 목록 = 로그아웃 처리 될 정보들
        return getSessions(tokenAuthInfo)
    }

    private fun getSessions(tokenAuthInfo: TokenAuthInfo): List<SessionInfo> {
        val refreshTokenKey = getRefreshTokenKey(
            role = tokenAuthInfo.role,
            userId = tokenAuthInfo.userId
        )

        val allSessionEntries = redisTemplate.opsForHash<String, String>().entries(refreshTokenKey)
        if (allSessionEntries.isEmpty()) {
            throw BusinessErrorCode.UNAUTHORIZED.exception("이미 모든 기기에서 로그아웃 처리 되었습니다")
        }

        val now = Instant.now()
        val validSessions = mutableListOf<SessionInfo>()
        val expiredDeviceIds = mutableListOf<String>()

        allSessionEntries.forEach { (deviceId, sessionInfoJson) ->
            val refreshTokenInfo = objectMapper.readValue(sessionInfoJson, RefreshTokenInfo::class.java)

            // 만료 시간을 현재와 비교하여 유효한 세션만 골라냄
            if (refreshTokenInfo.expiresAt.isAfter(now)) {
                validSessions.add(
                    SessionInfo(
                        deviceId = deviceId,
                        loginDateTime = refreshTokenInfo.loginAt.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(),
                        lastActivityDateTime = refreshTokenInfo.lastActivityAt.atZone(ZoneId.of("Asia/Seoul"))
                            .toLocalDateTime()
                    )
                )
            } else {
                // 만료된 세션은 정리 목록에 추가
                expiredDeviceIds.add(deviceId)
            }
        }

        // 만료된 유령 세션을 DB에서 삭제하여 정리
        if (expiredDeviceIds.isNotEmpty()) {
            redisTemplate.opsForHash<String, String>().delete(refreshTokenKey, *expiredDeviceIds.toTypedArray())
            redisTemplate.opsForZSet()
                .remove(getSessionKey(tokenAuthInfo.role, tokenAuthInfo.userId), *expiredDeviceIds.toTypedArray())
        }

        return validSessions
    }

    override fun checkActiveJti(): TokenAuthInfo {
        val accessToken = requestContext.getAccessToken()
        val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(accessToken)

        val activeJtiKey = getActiveJtiKey(tokenAuthInfo.jti)

        redisTemplate.opsForValue().get(activeJtiKey)
            ?: throw BusinessErrorCode.UNAUTHORIZED.exception("로그아웃 처리 된 인증 정보입니다. 다시 로그인 해주세요.")

        return tokenAuthInfo
    }

    fun logout(deviceId: String? = null): SessionInfo {
        val now = Instant.now()
        // 1. AccessToken 유효성 검사
        val accessToken = requestContext.getAccessToken()
        val claims = try {
            jwtTokenProvider.getClaims(accessToken)
        } catch (expiredJwtException: ExpiredJwtException) {
            // AccessToken 만료된 경우에도 로그아웃 처리를 위해 예외에서 정보 추출
            expiredJwtException.claims
        }
        val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(claims)
        if (tokenAuthInfo.deviceId == deviceId) {
            throw BusinessErrorCode.BAD_REQUEST.exception("다른 기기 로그아웃 요청 시, 현재 기기 정보 요청은 유효하지 않습니다.")
        }
        // 현재 접속 기기가 아닌 다른 접속 기기의 로그아웃을 요청한 경우, 활성화된 토큰인지 한번 더 검증
        var logoutDeviceId = tokenAuthInfo.deviceId
        if (deviceId != null) {
            checkActiveJti()
            logoutDeviceId = deviceId
        }

        // 2. 로그아웃 요청한 Device ID가 로그인 중인 기기 정보가 맞는지 검사 (현재 접속 중인 기기와 로그아웃 요청한 기기가 다를 수 있음을 가정)
        val refreshTokenKey = getRefreshTokenKey(tokenAuthInfo.role, tokenAuthInfo.userId)
        val refreshTokenInfo = checkLoginDeviceIdThrow(
            deviceId = logoutDeviceId,
            refreshTokenKey = refreshTokenKey
        )

        // 3. Lua 스크립트 실행을 위한 파라미터 준비 (로그아웃 요청 된 AccessToken, RefreshToken 무력화)
        val sessionAgesKey = getSessionKey(
            role = tokenAuthInfo.role,
            userId = tokenAuthInfo.userId,
        )
        val activeJtiKey = getActiveJtiKey(refreshTokenInfo.accessTokenJti)

        // --- 원자적 스크립트 실행 ---
        redisTemplate.execute(
            logoutScript,
            listOf(refreshTokenKey, sessionAgesKey, activeJtiKey),
            logoutDeviceId
        )

        return SessionInfo(
            deviceId = logoutDeviceId,
            loginDateTime = refreshTokenInfo.loginAt.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(),
            lastActivityDateTime = refreshTokenInfo.lastActivityAt.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(),
        )
    }

    private fun checkLoginDeviceIdThrow(deviceId: String, refreshTokenKey: String): RefreshTokenInfo {
        val refreshTokenInfoString = redisTemplate.opsForHash<String, String>().get(refreshTokenKey, deviceId)
            ?: throw BusinessErrorCode.UNAUTHORIZED.exception("$deviceId 기기 접속 세션 정보가 존재하지 않습니다.")

        return objectMapper.readValue(refreshTokenInfoString, RefreshTokenInfo::class.java)
    }

    fun logoutAll(): List<SessionInfo> {
        // 1. AccessToken 유효성 검사 (모든 기기 로그아웃 요청은 만료 또는 이미 로그아웃 처리된 토큰인지 검증)
        val tokenAuthInfo = checkActiveJti()
        // 2. 리프레쉬 토큰 세션 Hash 조회 -> 현재 접속 중인 로그인 기기 목록 = 로그아웃 처리 될 정보들
        val sessionInfos = getSessions(tokenAuthInfo)


        // 3. Lua 스크립트 실행을 위한 파라미터 준비 (해당 유저의 모든 AccessToken, RefreshToken 무력화)
        val refreshTokenKey = getRefreshTokenKey(
            role = tokenAuthInfo.role,
            userId = tokenAuthInfo.userId
        )
        val sessionAgesKey = getSessionKey(
            role = tokenAuthInfo.role,
            userId = tokenAuthInfo.userId
        )

        // --- 원자적 스크립트 실행 ---
        redisTemplate.execute(
            logoutAllScript,
            listOf(refreshTokenKey, sessionAgesKey),
            UUID.randomUUID().toString()
        )

        return sessionInfos
    }

}

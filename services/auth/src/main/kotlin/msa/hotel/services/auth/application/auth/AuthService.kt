package msa.hotel.services.auth.application.auth

import msa.hotel.modules.jwt.token.JwtDecoder
import msa.hotel.modules.jwt.token.JwtProvider
import msa.hotel.modules.jwt.token.dto.Token
import msa.hotel.modules.jwt.token.dto.TokenUserInfo
import msa.hotel.modules.web.exception.ErrorCode
import msa.hotel.services.auth.application.auth.command.LoginCommand
import msa.hotel.services.auth.application.auth.dto.AuthTokenDto
import msa.hotel.services.auth.application.auth.dto.SessionInfoDto
import msa.hotel.services.auth.domain.auth.model.RefreshTokenInfo
import msa.hotel.services.auth.domain.auth.policy.MaximumLoginClientPolicy
import msa.hotel.services.auth.domain.auth.policy.TokenExpirationPolicy.ACCESS_TOKEN_EXPIRATION_IN_HOURS
import msa.hotel.services.auth.domain.auth.policy.TokenExpirationPolicy.REFRESH_TOKEN_EXPIRATION_IN_HOURS
import msa.hotel.services.auth.domain.auth.port.AuthTokenRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.util.Date

@Service
class AuthService(
    private val authIdentityService: AuthIdentityService,
    private val jwtProvider: JwtProvider,
    private val tokenRepo: AuthTokenRepository,
    private val jwtDecoder: JwtDecoder,
) {
    fun login(command: LoginCommand): AuthTokenDto {
        // 1. 유저 신원 인증
        val identity = authIdentityService.validateLogin(command)
        val userInfo =
            TokenUserInfo(
                role = command.role.name,
                userId = identity.id.value,
                deviceId = command.deviceId,
            )

        // 2. 같은 기기 중복 로그인 체크
        val pastLoginJti: String? = checkDuplicateDeviceLogin(userInfo)

        // 3. 인증 토큰 생성 및 저장
        val authToken = generateAuthToken(userInfo, pastActiveJti = pastLoginJti)

        // 4. identity 로그인 성공 업데이트
        authIdentityService.updateLoginSuccess(identity, loginAt = authToken.refreshTokenIssuedAt)

        return authToken
    }

    private fun checkDuplicateDeviceLogin(userInfo: TokenUserInfo): String? {
        val savedLoginTokenInfo: RefreshTokenInfo = tokenRepo.findRefreshTokenInfo(userInfo) ?: return null

        return savedLoginTokenInfo.accessTokenJti
    }

    private fun generateAuthToken(
        userInfo: TokenUserInfo,
        loginAt: Instant = Instant.now(),
        pastActiveJti: String? = null,
    ): AuthTokenDto {
        // 1. AccessToken 생성
        val issuedAt = Instant.now()
        val accessTokenExpiration = issuedAt.plus(Duration.ofHours(ACCESS_TOKEN_EXPIRATION_IN_HOURS))
        val accessToken: Token =
            jwtProvider.generate(
                issuedAt = Date.from(issuedAt),
                expiration = Date.from(accessTokenExpiration),
                userInfo,
            )

        // 2. RefreshToken 생성
        val refreshTokenExpiration = issuedAt.plus(Duration.ofHours(REFRESH_TOKEN_EXPIRATION_IN_HOURS))
        val refreshToken: Token =
            jwtProvider.generate(
                issuedAt = Date.from(issuedAt),
                expiration = Date.from(refreshTokenExpiration),
                userInfo,
            )
        val refreshTokenInfo =
            RefreshTokenInfo(
                accessTokenJti = accessToken.jti,
                token = refreshToken.value,
                loginAt = loginAt,
                lastActivityAt = issuedAt,
                expiresAt = refreshTokenExpiration,
            )

        // 로그인 인증 토큰 정보 저장
        tokenRepo.saveAuthTokens(
            userInfo = userInfo,
            issuedAt = issuedAt,
            accessTokenExpiration = accessTokenExpiration,
            refreshTokenInfo = refreshTokenInfo,
            refreshTokenExpiration = refreshTokenExpiration,
            maxLoginClient = MaximumLoginClientPolicy.MAX_CLIENT,
            pastActiveJti = pastActiveJti,
        )

        return AuthTokenDto(
            accessToken = accessToken.value,
            refreshToken = refreshToken.value,
            refreshTokenIssuedAt = issuedAt,
            refreshTokenExpiration = refreshTokenExpiration,
        )
    }

    fun reissueToken(
        accessToken: String,
        refreshToken: String,
    ): AuthTokenDto {
        // 1. AccessToken 유효성 검사
        val (tokenAuthInfo, isExpired) = jwtDecoder.extractAuthInfoCathExpired(accessToken)

        // 2. 만료된 AccessToken 토큰이 아닌 경우, 토큰 JTI가 유효해야함
        if (!isExpired && !tokenRepo.existActiveJti(tokenAuthInfo.jti)) {
            throw ErrorCode.UNAUTHORIZED.exception("로그아웃 처리 된 인증 정보입니다. 다시 로그인 해주세요.")
        }

        // 3. RefreshToken 유효성 검사
        val refreshTokenInfo =
            tokenRepo.findRefreshTokenInfo(tokenAuthInfo.tokenUserInfo) ?: throw ErrorCode.UNAUTHORIZED.exception("인증 세션 정보가 존재하지 않습니다.")

        if (refreshTokenInfo.token != refreshToken) {
            throw ErrorCode.UNAUTHORIZED.exception("인증 정보가 올바르지 않습니다.")
        }

        // 4. 인증 토큰 재발급
        return generateAuthToken(
            tokenAuthInfo.tokenUserInfo,
            refreshTokenInfo.loginAt,
            pastActiveJti = tokenAuthInfo.jti,
        )
    }

    fun logout(
        accessToken: String,
        deviceId: String? = null,
    ): SessionInfoDto {
        // 현재 접속 기기가 아닌 다른 접속 기기의 로그아웃을 요청한 경우, 만료안된 토큰만 허용
        // 1. AccessToken 유효성 검사
        val tokenAuthInfo =
            if (deviceId != null) {
                jwtDecoder.extractAuthInfo(accessToken)
            } else {
                jwtDecoder.extractAuthInfoCathExpired(accessToken).first
            }
        val tokenDeviceId = tokenAuthInfo.tokenUserInfo.deviceId

        if (tokenDeviceId == deviceId) {
            throw ErrorCode.BAD_REQUEST.exception("다른 기기 로그아웃 요청 시, 현재 기기 정보 요청은 유효하지 않습니다.")
        }

        // 현재 접속 기기가 아닌 다른 접속 기기의 로그아웃을 요청한 경우, 지금 세션이 활성화된 정보인지 한번 더 검증
        var logoutDeviceId = tokenDeviceId
        if (deviceId != null) {
            logoutDeviceId = deviceId
            if (!tokenRepo.existActiveJti(tokenAuthInfo.jti)) {
                throw ErrorCode.UNAUTHORIZED.exception("현재 세션이 유효하지 않아 다른 세션을 로그아웃할 수 없습니다")
            }
        }

        // 2. 로그아웃 요청한 Device ID가 로그인 중인 기기 정보가 맞는지 검사
        val logoutRefreshTokenInfo =
            tokenRepo.findRefreshTokenInfo(tokenAuthInfo.tokenUserInfo, logoutDeviceId)
                ?: throw ErrorCode.UNAUTHORIZED.exception("로그아웃 요청한 $logoutDeviceId 기기 인증 정보가 존재하지 않습니다. 이미 로그아웃 된 상태입니다.")

        // 3. 로그아웃 Lua 스크립트 실행 (로그아웃 요청 된 AccessToken, RefreshToken 무력화)
        tokenRepo.deleteAuthTokenByLogout(
            tokenAuthInfo.tokenUserInfo,
            logoutActiveJti = logoutRefreshTokenInfo.accessTokenJti,
            logoutDeviceId = logoutDeviceId,
        )

        return SessionInfoDto(
            deviceId = logoutDeviceId,
            loginDateTime = logoutRefreshTokenInfo.loginAt.atZone(UTC).toLocalDateTime(),
            lastActivityDateTime = logoutRefreshTokenInfo.lastActivityAt.atZone(UTC).toLocalDateTime(),
        )
    }

    fun logoutAll(accessToken: String): List<SessionInfoDto> {
        // 1. AccessToken 유효성 검사
        val tokenAuthInfo = jwtDecoder.extractAuthInfo(accessToken)
        if (!tokenRepo.existActiveJti(tokenAuthInfo.jti)) {
            throw ErrorCode.UNAUTHORIZED.exception("현재 세션이 유효하지 않아 모든 세션을 로그아웃할 수 없습니다")
        }

        // 2. 모든 접속 세션 내역 확인
        val sessionInfos = getSessions(tokenAuthInfo.tokenUserInfo)
        if (sessionInfos.isEmpty()) {
            throw ErrorCode.CONFLICT.exception("이미 모두 로그아웃 처리 되었습니다.")
        }

        // 3. 모든 기기 로그아웃 Lua 스크립트 실행 (로그아웃 요청 된 AccessToken, RefreshToken 무력화)
        tokenRepo.deleteAuthTokenByLogoutAll(tokenAuthInfo.tokenUserInfo)

        return sessionInfos
    }

    fun getSessions(accessToken: String): List<SessionInfoDto> {
        val tokenAuthInfo = jwtDecoder.extractAuthInfo(accessToken)
        if (!tokenRepo.existActiveJti(tokenAuthInfo.jti)) {
            throw ErrorCode.UNAUTHORIZED.exception("현재 세션이 유효하지 않아 모든 세션을 로그아웃할 수 없습니다")
        }

        return getSessions(tokenAuthInfo.tokenUserInfo)
    }

    fun getSessions(userInfo: TokenUserInfo): List<SessionInfoDto> {
        val refreshTokenInfos = tokenRepo.findAllRefreshTokenInfo(userInfo)

        return refreshTokenInfos.map {
            val authInfo = jwtDecoder.extractAuthInfo(it.token)
            SessionInfoDto(
                deviceId = authInfo.tokenUserInfo.deviceId,
                loginDateTime = it.loginAt.atZone(UTC).toLocalDateTime(),
                lastActivityDateTime = it.lastActivityAt.atZone(UTC).toLocalDateTime(),
            )
        }
    }
}

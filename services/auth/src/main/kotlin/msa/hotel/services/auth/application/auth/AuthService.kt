package msa.hotel.services.auth.application.auth

import msa.hotel.modules.jwt.token.JwtDecoder
import msa.hotel.modules.jwt.token.JwtProvider
import msa.hotel.modules.jwt.token.dto.Token
import msa.hotel.modules.jwt.token.dto.TokenUserInfo
import msa.hotel.services.auth.application.auth.command.LoginCommand
import msa.hotel.services.auth.application.auth.dto.AuthTokenDto
import msa.hotel.services.auth.domain.auth.model.RefreshTokenInfo
import msa.hotel.services.auth.domain.auth.policy.MaximumLoginClientPolicy
import msa.hotel.services.auth.domain.auth.policy.TokenExpirationPolicy.ACCESS_TOKEN_EXPIRATION_IN_HOURS
import msa.hotel.services.auth.domain.auth.policy.TokenExpirationPolicy.REFRESH_TOKEN_EXPIRATION_IN_HOURS
import msa.hotel.services.auth.domain.auth.port.AuthTokenRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
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
}

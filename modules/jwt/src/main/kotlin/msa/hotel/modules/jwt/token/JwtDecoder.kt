package msa.hotel.modules.jwt.token

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import msa.hotel.modules.jwt.config.properties.JwtProperties
import msa.hotel.modules.jwt.token.dto.TokenAuthInfo
import msa.hotel.modules.jwt.token.dto.TokenUserInfo
import msa.hotel.modules.web.exception.ErrorCode
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

private val logger = KotlinLogging.logger {}

@Component
class JwtDecoder(
    private val jwtProperties: JwtProperties,
) {
    fun getClaims(token: String): Claims {
        val key = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8))

        return Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun extractAuthInfoCathExpired(token: String): Pair<TokenAuthInfo, Boolean> {
        var isExpired = false
        var claims: Claims? = null
        try {
            claims = getClaims(token)
        } catch (expiredJwtException: ExpiredJwtException) {
            // AccessToken 만료된 경우에도 토큰 재발급을 위해 예외에서 정보 추출claims
            claims = expiredJwtException.claims
            isExpired = true
        }

        return Pair(extractAuthInfo(claims), isExpired)
    }

    fun extractAuthInfo(token: String): TokenAuthInfo {
        try {
            val claims = getClaims(token)

            return extractAuthInfo(claims)
        } catch (e: Exception) {
            logger.error { "Extract AuthInfo exception (token): $e" }
            throw ErrorCode.UNAUTHORIZED.exception("인증 정보가 올바르지 않습니다.")
        }
    }

    fun extractAuthInfo(claims: Claims): TokenAuthInfo {
        try {
            val jti = claims.id
            val userId = claims.subject.toLong().toULong()
            val role = claims.get("role", String::class.java)
            val expiration = claims.expiration
            val deviceId = claims.get("deviceId", String::class.java)
            val tokenUserInfo =
                TokenUserInfo(
                    role = role,
                    userId = userId,
                    deviceId = deviceId,
                )

            return TokenAuthInfo(
                jti = jti,
                expiration = expiration,
                tokenUserInfo = tokenUserInfo,
            )
        } catch (e: Exception) {
            logger.error { "Extract AuthInfo exception (Claims): $e" }
            throw ErrorCode.UNAUTHORIZED.exception("인증 정보가 올바르지 않습니다.")
        }
    }
}

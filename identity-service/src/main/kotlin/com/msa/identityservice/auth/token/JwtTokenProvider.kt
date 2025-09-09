package com.msa.identityservice.auth.token

import com.msa.identityservice.auth.token.dto.TokenAuthInfo
import com.msa.identityservice.auth.token.enums.Role
import com.msa.identityservice.config.properties.JwtProperties
import com.msa.identityservice.exception.BusinessErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*


private val logger = KotlinLogging.logger {}

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {

    fun generateJsonWebToken(
        issuedAt: Date,
        expiration: Date,
        userId: Long,
        email: String,
        role: Role,
        deviceId: String
    ): String {
        val key = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8))

        return Jwts.builder()
            .header()
            .and()
            .issuer("identity-service")
            .issuedAt(issuedAt)
            .expiration(expiration)
            .subject(userId.toString())
            .claim("role", role.name)
            .claim("email", email)
            .claim("deviceId", deviceId)
            .signWith(key)
            .compact()
    }

    fun extractAuthInfo(token: String): TokenAuthInfo {
        try {
            val claims = getClaims(token)

            return extractAuthInfo(claims)
        } catch (e: Exception) {
            logger.error { "Extract AuthInfo exception: $e" }
            throw BusinessErrorCode.UNAUTHORIZED.exception("인증 정보가 올바르지 않습니다.")
        }
    }

    private fun getClaims(token: String): Claims {
        val key = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8))

        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun extractAuthInfo(claims: Claims): TokenAuthInfo {
        val userId = claims.subject.toLong()
        val role = claims.get("role", String::class.java)
        val email = claims.get("email", String::class.java)
        val expiration = claims.expiration
        val deviceId = claims.get("deviceId", String::class.java)

        return TokenAuthInfo(
            userId = userId,
            email = email,
            role = Role.valueOf(role),
            expiresAt = expiration,
            deviceId = deviceId
        )
    }

    fun extractAuthInfo(expiredJwtException: ExpiredJwtException): TokenAuthInfo {
        try {
            val claims = expiredJwtException.claims

            return extractAuthInfo(claims)
        } catch (e: Exception) {
            logger.error { "Extract AuthInfo exception (ExpiredJwtException): $e" }
            throw BusinessErrorCode.UNAUTHORIZED.exception("인증 정보가 올바르지 않습니다.")
        }
    }
}

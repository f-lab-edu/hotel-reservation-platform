package com.msa.gatewayservice.auth.token

import com.msa.gatewayservice.auth.token.dto.TokenAuthInfo
import com.msa.gatewayservice.config.properties.JwtProperties
import com.msa.gatewayservice.exception.BusinessErrorCode
import com.msa.identityservice.auth.token.enums.Role
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets


private val logger = KotlinLogging.logger {}

@Component
class JwtTokenDecoder(
    private val jwtProperties: JwtProperties
) {

    private fun getClaims(token: String): Claims {
        val key = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8))

        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun extractAuthInfo(token: String): TokenAuthInfo {
        try {
            val claims = getClaims(token)

            return extractAuthInfo(claims)
        } catch (e: Exception) {
            logger.error { "Extract AuthInfo exception (token): $e" }
            throw BusinessErrorCode.UNAUTHORIZED.exception("인증 정보가 올바르지 않습니다.")
        }
    }

    private fun extractAuthInfo(claims: Claims): TokenAuthInfo {
        try {
            val jti = claims.id
            val userId = claims.subject.toLong()
            val role = claims.get("role", String::class.java)
            val email = claims.get("email", String::class.java)
            val expiration = claims.expiration
            val deviceId = claims.get("deviceId", String::class.java)

            return TokenAuthInfo(
                jti = jti,
                userId = userId,
                email = email,
                role = Role.valueOf(role),
                expiresAt = expiration,
                deviceId = deviceId
            )
        } catch (e: Exception) {
            logger.error { "Extract AuthInfo exception (Claims): $e" }
            throw BusinessErrorCode.UNAUTHORIZED.exception("인증 정보가 올바르지 않습니다.")
        }
    }

}

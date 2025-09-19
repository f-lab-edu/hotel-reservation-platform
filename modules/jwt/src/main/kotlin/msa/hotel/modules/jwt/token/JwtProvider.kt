package msa.hotel.modules.jwt.token

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import msa.hotel.modules.jwt.config.properties.JwtProperties
import msa.hotel.modules.jwt.token.dto.Token
import msa.hotel.modules.jwt.token.dto.TokenUserInfo
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.UUID

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    fun generate(
        issuedAt: Date,
        expiration: Date,
        userInfo: TokenUserInfo,
    ): Token {
        val key = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8))
        val jti = UUID.randomUUID().toString()

        val jwt =
            Jwts
                .builder()
                .header()
                .and()
                .id(jti)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .subject(userInfo.userId.toString())
                .claim("role", userInfo.role)
                .claim("deviceId", userInfo.deviceId)
                .signWith(key)
                .compact()

        return Token(
            value = jwt,
            jti = jti,
        )
    }
}

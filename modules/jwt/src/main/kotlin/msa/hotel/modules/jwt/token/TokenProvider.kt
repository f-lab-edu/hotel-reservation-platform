package msa.hotel.modules.jwt.token

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import msa.hotel.modules.jwt.config.properties.JwtProperties
import msa.hotel.modules.jwt.enums.Role
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class TokenProvider(
    private val jwtProperties: JwtProperties
) {

    fun generateJsonWebToken(
        jti: String,
        issuedAt: Date,
        expiration: Date,
        userId: Long,
        role: Role,
        deviceId: String
    ): String {
        val key = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8))

        return Jwts.builder()
            .header()
            .and()
            .id(jti)
            .issuedAt(issuedAt)
            .expiration(expiration)
            .subject(userId.toString())
            .claim("role", role.name)
            .claim("deviceId", deviceId)
            .signWith(key)
            .compact()
    }

}

package com.reservation.auth.token;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.reservation.auth.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
	private final JwtProperties jwtProperties;

	public String generateToken(Long userId, String role) {
		return createToken(userId, role, jwtProperties.getAccessTokenExpiry());
	}

	public String generateRefreshToken(Long userId, String role) {
		return createToken(userId, role, jwtProperties.getRefreshTokenExpiry());
	}

	private String createToken(Long userId, String role, long validityMillis) {
		Claims claims = Jwts.claims().setSubject(userId.toString());
		claims.put("role", role);

		Date now = new Date();
		Date expiry = new Date(now.getTime() + validityMillis);

		return Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(now)
			.setExpiration(expiry)
			.signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
			.compact();
	}

	public boolean isTokenValid(String token) {
		Jwts.parser()
			.setSigningKey(jwtProperties.getSecretKey())
			.parseClaimsJws(token);
		return true;
	}

	private Claims getClaim(String token) {
		return Jwts.parser()
			.setSigningKey(jwtProperties.getSecretKey())
			.parseClaimsJws(token)
			.getBody();
	}

	public Long extractUserId(String token) {
		return Long.parseLong(getClaim(token).getSubject());
	}

	public String extractRole(String token) {
		return getClaim(token).get("role", String.class);
	}

	public Date extractExpiration(String token) {
		return getClaim(token).getExpiration();
	}
}

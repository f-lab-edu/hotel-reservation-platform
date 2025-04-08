package com.reservation.commonauth.auth;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenProvider {
	private static final long ACCESS_TOKEN_VALIDITY_IN_MILLIS = 60 * 60 * 1000L; // 1시간
	private static final long REFRESH_TOKEN_VALIDITY_IN_MILLIS = 7 * 24 * 60 * 60 * 1000L; // 7일

	private final String secretKey;

	public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey) {
		this.secretKey = secretKey;
	}

	public String generateToken(Long userId, String role) {
		return createToken(userId, role, ACCESS_TOKEN_VALIDITY_IN_MILLIS);
	}

	public String generateRefreshToken(Long userId, String role) {
		return createToken(userId, role, REFRESH_TOKEN_VALIDITY_IN_MILLIS);
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
			.signWith(SignatureAlgorithm.HS256, secretKey)
			.compact();
	}

	public boolean isTokenValid(String token) {
		try {
			Jwts.parser()
				.setSigningKey(secretKey)
				.parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public Long extractUserId(String token) {
		return Long.valueOf(
			Jwts.parser()
				.setSigningKey(secretKey)
				.parseClaimsJws(token)
				.getBody()
				.getSubject()
		);
	}

	public String extractRole(String token) {
		return Jwts.parser()
			.setSigningKey(secretKey)
			.parseClaimsJws(token)
			.getBody()
			.get("role", String.class);
	}
}

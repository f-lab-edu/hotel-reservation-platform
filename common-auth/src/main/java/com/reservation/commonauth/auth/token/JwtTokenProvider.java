package com.reservation.commonauth.auth.token;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.reservation.commonauth.auth.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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
		try {
			Jwts.parser()
				.setSigningKey(jwtProperties.getSecretKey())
				.parseClaimsJws(token);
			return true;
		} catch (ExpiredJwtException e) {
			throw e;
		} catch (Exception e) {
			return false;
		}
	}

	public Long extractUserId(String token) {
		try {
			return Long.parseLong(
				Jwts.parser()
					.setSigningKey(jwtProperties.getSecretKey())
					.parseClaimsJws(token)
					.getBody()
					.getSubject()
			);
		} catch (ExpiredJwtException e) {
			return Long.parseLong(e.getClaims().getSubject());
		} catch (Exception e) {
			return null;
		}
	}

	public String extractRole(String token) {
		return Jwts.parser()
			.setSigningKey(jwtProperties.getSecretKey())
			.parseClaimsJws(token)
			.getBody()
			.get("role", String.class);
	}

	public Date extractExpiration(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(jwtProperties.getSecretKey())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getExpiration();
		} catch (ExpiredJwtException e) {
			return null;
		}
	}
}

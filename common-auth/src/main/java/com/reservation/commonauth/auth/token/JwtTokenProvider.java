package com.reservation.commonauth.auth.token;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;

@Component
public class JwtTokenProvider {
	@Value("${jwt.secret-key}")
	private String secretKey;

	@Value("${jwt.access-token-expiry}")
	private long accessTokenValidityInMilliSeconds;

	@Getter
	@Value("${jwt.refresh-token-expiry}")
	private long refreshTokenValidityInMilliSeconds;

	public String generateToken(Long userId, String role) {
		return createToken(userId, role, accessTokenValidityInMilliSeconds);
	}

	public String generateRefreshToken(Long userId, String role) {
		return createToken(userId, role, refreshTokenValidityInMilliSeconds);
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
					.setSigningKey(secretKey)
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
			.setSigningKey(secretKey)
			.parseClaimsJws(token)
			.getBody()
			.get("role", String.class);
	}

	public Date extractExpiration(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getExpiration();
		} catch (ExpiredJwtException e) {
			return null;
		}
	}
}

package com.reservation.commonauth.auth.filter;

import static com.reservation.commonauth.auth.login.BlacklistService.*;
import static com.reservation.commonauth.auth.token.RequestContext.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.common.response.ApiErrorResponse;
import com.reservation.commonauth.auth.config.JwtProperties;
import com.reservation.commonauth.auth.token.JwtTokenProvider;
import com.reservation.commonmodel.exception.ErrorCode;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	@Value("${jwt.skip-urls}")
	private Set<String> SKIP_PATHS;

	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return SKIP_PATHS.stream().anyMatch(uri::startsWith);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String token = resolveToken(request);

		// 블랙리스트 체크
		if (token != null && checkBlacklistToken(token, response)) {
			return;
		}

		try {
			if (token != null && jwtTokenProvider.isTokenValid(token)) {
				Long memberId = jwtTokenProvider.extractUserId(token);
				String role = jwtTokenProvider.extractRole(token);
				UsernamePasswordAuthenticationToken auth =
					new UsernamePasswordAuthenticationToken(memberId, null, List.of(new SimpleGrantedAuthority(role)));

				SecurityContextHolder.getContext().setAuthentication(auth);
				filterChain.doFilter(request, response);
				return;
			}
		} catch (ExpiredJwtException e) {
			response.setContentType("application/json");
			response.getWriter()
				.write(new ObjectMapper().writeValueAsString(
					ApiErrorResponse.of(ErrorCode.UNAUTHORIZED.name(), "Token expired")));
			return;
		}

		response.setContentType("application/json");
		response.getWriter()
			.write(new ObjectMapper().writeValueAsString(
				ApiErrorResponse.of(ErrorCode.UNAUTHORIZED.name(), "Token invalid")));
		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTH_HEADER_NAME);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AUTH_HEADER_PREFIX)) {
			return bearerToken.substring(7); // "Bearer " 이후 토큰만 추출
		}
		return null;
	}

	private boolean checkBlacklistToken(String token, HttpServletResponse response) throws IOException {
		if (redisTemplate.hasKey(BLACKLIST_TOKEN_PREFIX + token)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter()
				.write(new ObjectMapper().writeValueAsString(
					ApiErrorResponse.of(ErrorCode.UNAUTHORIZED.name(), "Token blacklisted")));
			return true;
		}
		return false;
	}
}

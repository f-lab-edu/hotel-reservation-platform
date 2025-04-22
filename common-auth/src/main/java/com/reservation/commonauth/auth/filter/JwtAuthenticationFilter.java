package com.reservation.commonauth.auth.filter;

import static com.reservation.commonauth.auth.login.BlacklistService.*;
import static com.reservation.commonauth.auth.token.RequestContext.*;

import java.io.IOException;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.reservation.commonauth.auth.security.AuthErrorType;
import com.reservation.commonauth.auth.token.JwtTokenProvider;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String token = resolveToken(request);

		//❌인증 토큰이 없는 경우
		if (token == null) {
			request.setAttribute("authError", AuthErrorType.MISSING_TOKEN);
			filterChain.doFilter(request, response);
			return;
		}

		//❌블랙리스트로 등록된 토큰인지 확인
		if (redisTemplate.hasKey(BLACKLIST_TOKEN_PREFIX + token)) {
			request.setAttribute("authError", AuthErrorType.BLACKLIST_TOKEN);
			filterChain.doFilter(request, response);
			return;
		}

		try {
			//✅토큰이 유효한 경우
			jwtTokenProvider.isTokenValid(token);
			Long userId = jwtTokenProvider.extractUserId(token);
			String role = jwtTokenProvider.extractRole(token);
			UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority(role)));

			SecurityContextHolder.getContext().setAuthentication(auth);
			filterChain.doFilter(request, response);
		}
		//❌토큰이 만료된 경우
		catch (ExpiredJwtException e) {
			request.setAttribute("authError", AuthErrorType.INVALID_TOKEN);
			filterChain.doFilter(request, response);
		}
		// ❌토큰이 유효하지 않은 경우
		catch (Exception e) {
			request.setAttribute("authError", AuthErrorType.MALFORMED_TOKEN);
			filterChain.doFilter(request, response);
		}
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTH_HEADER_NAME);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AUTH_HEADER_PREFIX)) {
			return bearerToken.substring(7); // "Bearer " 이후 토큰만 추출
		}
		return null;
	}
}

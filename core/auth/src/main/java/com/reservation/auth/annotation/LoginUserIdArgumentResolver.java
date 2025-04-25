package com.reservation.auth.annotation;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.reservation.auth.token.JwtTokenProvider;
import com.reservation.auth.token.RequestContext;
import com.reservation.support.exception.ErrorCode;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginUserIdArgumentResolver implements HandlerMethodArgumentResolver {
	private final JwtTokenProvider jwtTokenProvider;
	private final RequestContext requestContext;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(LoginUserId.class) &&
			parameter.getParameterType().equals(long.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		String accessToken = requestContext.getAccessToken();
		if (accessToken == null || accessToken.isBlank()) {
			throw ErrorCode.UNAUTHORIZED.exception("인증 정보가 존재하지 않습니다.");
		}
		try {
			System.out.println("accessToken = " + accessToken);
			return jwtTokenProvider.extractUserId(accessToken);
		} catch (ExpiredJwtException e) {
			LoginUserId annotation = parameter.getParameterAnnotation(LoginUserId.class);
			// includeExpired = true 경우만 만료된 토큰 허용
			if (annotation != null && annotation.includeExpired()) {
				return Long.parseLong(e.getClaims().getSubject());
			}
			throw ErrorCode.UNAUTHORIZED.exception("인증 토큰이 만료되었습니다.");
		} catch (Exception e) {
			throw ErrorCode.UNAUTHORIZED.exception("인증 정보가 올바르지 않습니다.");
		}
	}
}

package com.reservation.commonauth.auth.annotation;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.reservation.commonauth.auth.token.JwtTokenProvider;
import com.reservation.commonauth.auth.token.RequestContext;
import com.reservation.commonmodel.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginUserIdArgumentResolver implements HandlerMethodArgumentResolver {
	private final JwtTokenProvider jwtTokenProvider;
	private final RequestContext requestContext;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(LoginUserId.class) &&
			parameter.getParameterType().equals(Long.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		String accessToken = requestContext.getAccessToken();
		if (accessToken == null || accessToken.isBlank()) {
			throw ErrorCode.UNAUTHORIZED.exception("인증 정보가 존재하지 않습니다.");
		}
		Long userId = jwtTokenProvider.extractUserId(accessToken);
		if (userId == null) {
			throw ErrorCode.UNAUTHORIZED.exception("인증 정보가 올바르지 않습니다.");
		}

		return userId;
	}
}

package com.reservation.auth.annotation;

import java.util.Collection;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.reservation.auth.annotation.dto.UserAuth;
import com.reservation.support.enums.Role;
import com.reservation.support.exception.ErrorCode;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(LoginUser.class) &&
			parameter.getParameterType().equals(UserAuth.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Long userId = Long.valueOf(authentication.getName());
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		Role role = authorities.stream()
			.map(GrantedAuthority::getAuthority)
			.map(roleStr -> roleStr.replace("ROLE_", ""))
			.map(Role::valueOf)
			.findFirst()
			.orElseThrow(() -> ErrorCode.UNAUTHORIZED.exception("권한이 없습니다."));

		return new UserAuth(userId, role);
	}
}

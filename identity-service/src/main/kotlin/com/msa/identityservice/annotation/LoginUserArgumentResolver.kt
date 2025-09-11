package com.msa.identityservice.annotation

import com.msa.identityservice.auth.service.ICheckActiveJtiService
import com.msa.identityservice.auth.token.dto.TokenAuthInfo
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer


@Component
class LoginUserArgumentResolver(
    private val checkActiveJtiService: ICheckActiveJtiService
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val hasParameterAnnotation = parameter.hasParameterAnnotation(LoginUser::class.java)
        val isTokenAuthInfo = parameter.parameterType == TokenAuthInfo::class.java

        return hasParameterAnnotation && isTokenAuthInfo
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): TokenAuthInfo {
        
        return checkActiveJtiService.checkActiveJti()
    }

}

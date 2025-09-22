package msa.hotel.services.auth.infrastructure.web.annotations.authinfo

import msa.hotel.modules.jwt.token.dto.TokenAuthInfo
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthInfoArgumentResolver(
    private val checkActiveJtiService: CheckActiveJtiService,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val hasParameterAnnotation = parameter.hasParameterAnnotation(AuthInfo::class.java)
        val isTokenAuthInfo = parameter.parameterType == TokenAuthInfo::class.java

        return hasParameterAnnotation && isTokenAuthInfo
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): TokenAuthInfo = checkActiveJtiService.checkActiveJti()
}

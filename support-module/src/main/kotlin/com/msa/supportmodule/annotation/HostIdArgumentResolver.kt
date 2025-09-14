package com.msa.supportmodule.annotation

import com.msa.supportmodule.auth.request.RequestContext
import com.msa.supportmodule.auth.token.enums.Role
import com.msa.supportmodule.exception.BusinessErrorCode
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class HostIdArgumentResolver(
    private val requestContext: RequestContext
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == Long::class.java && parameter.hasParameterAnnotation(HostId::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Long {
        val role = requestContext.getRole()
        if (role != Role.HOST) {
            throw BusinessErrorCode.FORBIDDEN.exception("허용되지 않은 요청입니다. 호스트만 요청 가능합니다.")
        }
        val hostId = requestContext.getUserId()

        return hostId
    }
}

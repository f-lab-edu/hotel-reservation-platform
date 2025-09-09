package com.msa.identityservice.annotation

import com.msa.identityservice.auth.service.AuthService.Companion.ACTIVE_KEY_PREFIX
import com.msa.identityservice.auth.token.JwtTokenProvider
import com.msa.identityservice.auth.token.RequestContext
import com.msa.identityservice.auth.token.dto.TokenAuthInfo
import com.msa.identityservice.auth.token.enums.Role
import com.msa.identityservice.exception.BusinessErrorCode
import org.springframework.core.MethodParameter
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer


@Component
class LoginUserArgumentResolver(
    private val requestContext: RequestContext,
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>
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
        val accessToken = requestContext.getAccessToken()
        val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(accessToken)

        checkActiveToken(
            role = tokenAuthInfo.role,
            userId = tokenAuthInfo.userId,
            deviceId = tokenAuthInfo.deviceId
        )

        return tokenAuthInfo
    }

    private fun checkActiveToken(
        role: Role,
        userId: Long,
        deviceId: String
    ) {
        redisTemplate.opsForValue().get("$ACTIVE_KEY_PREFIX:${role.name.lowercase()}:$userId:$deviceId")
            ?: throw BusinessErrorCode.UNAUTHORIZED.exception("로그아웃 처리 된 인증 정보입니다. 다시 로그인 해주세요.")
    }

}

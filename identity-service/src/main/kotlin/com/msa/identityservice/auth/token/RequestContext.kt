package com.msa.identityservice.auth.token

import com.msa.identityservice.exception.BusinessErrorCode
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import java.util.*


@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class RequestContext(
    private val request: HttpServletRequest
) {

    fun getRefreshToken(): String {
        if (request.cookies == null) {
            throw BusinessErrorCode.UNAUTHORIZED.exception(UNAUTHORIZED_MESSAGE)
        }

        return Arrays.stream(request.cookies)
            .filter { c -> c.name.equals(REFRESH_COOKIE_NAME) }
            .map { obj: Cookie -> obj.value }
            .findFirst()
            .orElseThrow { BusinessErrorCode.UNAUTHORIZED.exception(UNAUTHORIZED_MESSAGE) }
    }

    fun getAccessToken(): String {
        val authHeader = request.getHeader(AUTH_HEADER_NAME)
        if (authHeader == null || !authHeader.startsWith(AUTH_HEADER_PREFIX)) {
            throw BusinessErrorCode.UNAUTHORIZED.exception(UNAUTHORIZED_MESSAGE)
        }

        return authHeader.substring(AUTH_HEADER_PREFIX.length)
    }

    companion object {
        const val REFRESH_COOKIE_NAME = "refreshToken"
        const val AUTH_HEADER_NAME = "Authorization"
        const val AUTH_HEADER_PREFIX = "Bearer "
        const val UNAUTHORIZED_MESSAGE = "인증 정보가 존재하지 않습니다."
    }

}

package com.msa.identityservice.auth.token

import com.msa.identityservice.auth.consts.AuthConstants.AUTH_HEADER_NAME
import com.msa.identityservice.auth.consts.AuthConstants.AUTH_HEADER_PREFIX
import com.msa.identityservice.auth.consts.AuthConstants.CONTEXT_NOT_FOUND_MESSAGE
import com.msa.identityservice.auth.consts.AuthConstants.REFRESH_COOKIE_NAME
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
            throw BusinessErrorCode.UNAUTHORIZED.exception(CONTEXT_NOT_FOUND_MESSAGE)
        }

        return Arrays.stream(request.cookies)
            .filter { c -> c.name.equals(REFRESH_COOKIE_NAME) }
            .map { obj: Cookie -> obj.value }
            .findFirst()
            .orElseThrow { BusinessErrorCode.UNAUTHORIZED.exception(CONTEXT_NOT_FOUND_MESSAGE) }
    }

    fun getAccessToken(): String {
        val authHeader = request.getHeader(AUTH_HEADER_NAME)
        if (authHeader == null || !authHeader.startsWith(AUTH_HEADER_PREFIX)) {
            throw BusinessErrorCode.UNAUTHORIZED.exception(CONTEXT_NOT_FOUND_MESSAGE)
        }

        return authHeader.substring(AUTH_HEADER_PREFIX.length)
    }

}

package com.msa.supportmodule.auth.request

import com.msa.supportmodule.auth.consts.AuthConstants
import com.msa.supportmodule.auth.token.enums.Role
import com.msa.supportmodule.exception.BusinessErrorCode
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
            throw BusinessErrorCode.UNAUTHORIZED.exception(AuthConstants.CONTEXT_NOT_FOUND_MESSAGE)
        }

        return Arrays.stream(request.cookies)
            .filter { c -> c.name.equals(AuthConstants.REFRESH_COOKIE_NAME) }
            .map { obj: Cookie -> obj.value }
            .findFirst()
            .orElseThrow { BusinessErrorCode.UNAUTHORIZED.exception(AuthConstants.CONTEXT_NOT_FOUND_MESSAGE) }
    }

    fun getAccessToken(): String {
        val authHeader = request.getHeader(AuthConstants.AUTH_HEADER_NAME)
        if (authHeader == null || !authHeader.startsWith(AuthConstants.AUTH_HEADER_PREFIX)) {
            throw BusinessErrorCode.UNAUTHORIZED.exception(AuthConstants.CONTEXT_NOT_FOUND_MESSAGE)
        }

        return authHeader.substring(AuthConstants.AUTH_HEADER_PREFIX.length)
    }

    fun getUserId(): Long {
        val userIdHeader = request.getHeader(AuthConstants.USER_ID_HEADER_NAME)
        if (userIdHeader == null || userIdHeader.isBlank() || !userIdHeader.matches(Regex("\\d+"))) {
            throw BusinessErrorCode.UNAUTHORIZED.exception(AuthConstants.CONTEXT_NOT_FOUND_MESSAGE)
        }
        val userId = userIdHeader.toLong()

        return userId
    }

    fun getRole(): Role {
        val roleHeader = request.getHeader(AuthConstants.ROLE_HEADER_NAME)
        if (roleHeader == null || roleHeader.isBlank() || !Role.entries.any { it.name == roleHeader }) {
            throw BusinessErrorCode.UNAUTHORIZED.exception(AuthConstants.CONTEXT_NOT_FOUND_MESSAGE)
        }
        val role = Role.valueOf(roleHeader)

        return role
    }

    fun getEmail(): String {
        val emailHeader = request.getHeader(AuthConstants.ROLE_HEADER_NAME)
        if (emailHeader == null || emailHeader.isBlank() || !emailHeader.matches(Regex("\\d+"))) {
            throw BusinessErrorCode.UNAUTHORIZED.exception(AuthConstants.CONTEXT_NOT_FOUND_MESSAGE)
        }

        return emailHeader
    }
}

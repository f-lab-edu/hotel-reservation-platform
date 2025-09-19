package msa.hotel.services.auth.infrastructure.web.auth.header

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import msa.hotel.modules.web.exception.ErrorCode
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import java.util.Arrays

@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class RequestHeaderTokenExtractor(
    private val request: HttpServletRequest,
) {
    fun getRefreshToken(): String {
        if (request.cookies == null) {
            throw ErrorCode.UNAUTHORIZED.exception(HEADER_NOT_FOUND_MESSAGE)
        }

        return Arrays
            .stream(request.cookies)
            .filter { c -> c.name.equals(HeaderConstants.T_REFRESH_COOKIE_NAME) }
            .map { obj: Cookie -> obj.value }
            .findFirst()
            .orElseThrow { ErrorCode.UNAUTHORIZED.exception(HEADER_NOT_FOUND_MESSAGE) }
    }

    fun getAccessToken(): String {
        val authHeader = request.getHeader(HeaderConstants.T_ACCESS_HEADER_NAME)
        if (authHeader == null || !authHeader.startsWith(HeaderConstants.T_ACCESS_HEADER_PREFIX)) {
            throw ErrorCode.UNAUTHORIZED.exception(HEADER_NOT_FOUND_MESSAGE)
        }

        return authHeader.substring(HeaderConstants.T_ACCESS_HEADER_PREFIX.length)
    }

    companion object {
        const val HEADER_NOT_FOUND_MESSAGE = "인증 정보가 존재하지 않습니다."
    }
}

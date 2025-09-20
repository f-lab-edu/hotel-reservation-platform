package msa.hotel.services.auth.infrastructure.web.auth.header

import msa.hotel.modules.web.response.Response
import msa.hotel.services.auth.application.auth.dto.AuthTokenDto
import msa.hotel.services.auth.infrastructure.web.auth.header.HeaderConstants.T_ACCESS_HEADER_NAME
import msa.hotel.services.auth.infrastructure.web.auth.header.HeaderConstants.T_ACCESS_HEADER_PREFIX
import msa.hotel.services.auth.infrastructure.web.auth.header.HeaderConstants.T_REFRESH_COOKIE_NAME
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import java.time.Duration

fun setAuthTokenHeaders(authToken: AuthTokenDto): ResponseEntity<Unit> {
    val refreshTokenDuration = Duration.between(authToken.refreshTokenIssuedAt, authToken.refreshTokenExpiration)
    val responseCookie: ResponseCookie =
        ResponseCookie
            .from(T_REFRESH_COOKIE_NAME, authToken.refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(refreshTokenDuration)
            .build()

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .header(T_ACCESS_HEADER_NAME, makeAccessTokenHeaderValue(authToken.accessToken))
        .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        .header("Access-Control-Expose-Headers", T_ACCESS_HEADER_NAME)
        .build()
}

fun makeAccessTokenHeaderValue(accessToken: String): String = "$T_ACCESS_HEADER_PREFIX$accessToken"

fun <T> deleteAuthTokenHeaders(
    message: String,
    data: T,
): ResponseEntity<Response<T>> {
    val deleteCookie =
        ResponseCookie
            .from(T_REFRESH_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0)
            .build()

    val responseBody =
        Response.delete(
            message = message,
            data = data,
        )

    return ResponseEntity
        .ok()
        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
        .body(responseBody)
}

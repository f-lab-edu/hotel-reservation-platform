package com.msa.identityservice.auth.controller

import com.msa.identityservice.annotation.LoginUser
import com.msa.identityservice.auth.consts.AuthConstants.AUTH_HEADER_NAME
import com.msa.identityservice.auth.consts.AuthConstants.REFRESH_COOKIE_NAME
import com.msa.identityservice.auth.consts.AuthConstants.getAccessTokenHeaderValue
import com.msa.identityservice.auth.controller.request.LoginRequest
import com.msa.identityservice.auth.controller.request.LogoutRequest
import com.msa.identityservice.auth.controller.response.LogoutAllResponse
import com.msa.identityservice.auth.controller.response.SessionInfoResponse
import com.msa.identityservice.auth.controller.response.SessionInfosResponse
import com.msa.identityservice.auth.service.AuthService
import com.msa.identityservice.auth.token.dto.LoginAuthToken
import com.msa.identityservice.auth.token.dto.TokenAuthInfo
import com.msa.identityservice.support.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody
        request: LoginRequest
    ): ResponseEntity<Unit> {
        val loginRequestInfo = request.toLoginRequestInfo()
        val loginSettingToken = authService.login(loginRequestInfo)

        return setAuthTokenHeaders(loginSettingToken)
    }

    private fun setAuthTokenHeaders(loginAuthToken: LoginAuthToken): ResponseEntity<Unit> {
        val responseCookie: ResponseCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, loginAuthToken.refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(loginAuthToken.refreshTokenDuration)
            .build()

        return ResponseEntity.noContent()
            .header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(loginAuthToken.accessToken))
            .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
            .header("Access-Control-Expose-Headers", AUTH_HEADER_NAME)
            .build()
    }

    @GetMapping("/refresh")
    fun tokenReissue(): ResponseEntity<Unit> {
        val loginSettingToken = authService.tokenReissue()

        return setAuthTokenHeaders(loginSettingToken)
    }

    @GetMapping("/me")
    fun getMe(
        @LoginUser
        tokenAuthInfo: TokenAuthInfo
    ): ApiResponse<TokenAuthInfo> {

        return ApiResponse.read(
            message = "내 정보 조회에 성공했습니다.",
            data = tokenAuthInfo
        )
    }

    @GetMapping("/sessions")
    fun getSessions(): ApiResponse<SessionInfosResponse> {
        val sessionInfos = authService.getSessions()

        val sessions = sessionInfos.map { sessionInfo ->
            SessionInfoResponse(
                deviceId = sessionInfo.deviceId,
                loginDateTime = sessionInfo.loginDateTime,
                lastActivityDateTime = sessionInfo.lastActivityDateTime,
            )
        }

        return ApiResponse.read(
            message = "접속 중인 세션 조회에 성공했습니다.",
            data = SessionInfosResponse(sessions)
        )
    }

    @DeleteMapping("/logout")
    fun logout(): ResponseEntity<ApiResponse<SessionInfoResponse>> {
        val sessionInfo = authService.logout()

        val data = SessionInfoResponse(
            deviceId = sessionInfo.deviceId,
            loginDateTime = sessionInfo.loginDateTime,
            lastActivityDateTime = sessionInfo.lastActivityDateTime,
        )

        return deleteCookieResponse(
            message = "로그아웃 되었습니다.(${sessionInfo.deviceId})",
            data = data
        )
    }

    private fun <T> deleteCookieResponse(
        message: String,
        data: T
    ): ResponseEntity<ApiResponse<T>> {
        val deleteCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0)
            .build()

        val responseBody = ApiResponse.delete(
            message = message,
            data = data
        )

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
            .body(responseBody)
    }

    @DeleteMapping("/logout-device")
    fun logout(
        @Valid @RequestBody
        request: LogoutRequest
    ): ApiResponse<SessionInfoResponse> {
        val deviceId = request.deviceId!!
        val sessionInfo = authService.logout(deviceId)

        val data = SessionInfoResponse(
            deviceId = sessionInfo.deviceId,
            loginDateTime = sessionInfo.loginDateTime,
            lastActivityDateTime = sessionInfo.lastActivityDateTime,
        )

        return ApiResponse.delete(
            message = "로그아웃 되었습니다.($deviceId)",
            data = data
        )
    }

    @DeleteMapping("/logout-all")
    fun logoutAll(): ResponseEntity<ApiResponse<LogoutAllResponse>> {
        val sessionInfos = authService.logoutAll()
        val sessionInfoResponseList = sessionInfos.map { sessionInfo ->
            SessionInfoResponse(
                deviceId = sessionInfo.deviceId,
                loginDateTime = sessionInfo.loginDateTime,
                lastActivityDateTime = sessionInfo.lastActivityDateTime,
            )
        }

        return deleteCookieResponse(
            message = "접속 중인 모든 기기에서 로그아웃 되었습니다.",
            data = LogoutAllResponse(sessionInfoResponseList)
        )
    }

}

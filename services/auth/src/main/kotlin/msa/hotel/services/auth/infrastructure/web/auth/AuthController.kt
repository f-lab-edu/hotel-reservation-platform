package msa.hotel.services.auth.infrastructure.web.auth

import jakarta.validation.Valid
import msa.hotel.modules.web.response.Response
import msa.hotel.services.auth.application.auth.AuthService
import msa.hotel.services.auth.application.auth.dto.AuthTokenDto
import msa.hotel.services.auth.infrastructure.web.auth.dto.LoginRequest
import msa.hotel.services.auth.infrastructure.web.auth.dto.LogoutRequest
import msa.hotel.services.auth.infrastructure.web.auth.dto.SessionInfoResponse
import msa.hotel.services.auth.infrastructure.web.auth.header.RequestHeaderTokenExtractor
import msa.hotel.services.auth.infrastructure.web.auth.header.deleteAuthTokenHeaders
import msa.hotel.services.auth.infrastructure.web.auth.header.setAuthTokenHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val tokenExtractor: RequestHeaderTokenExtractor,
) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody
        request: LoginRequest,
    ): ResponseEntity<Unit> {
        val command = request.toLoginCommand()
        val authToken: AuthTokenDto = authService.login(command)

        return setAuthTokenHeaders(authToken)
    }

    @PostMapping("/reissue")
    fun reissueToken(): ResponseEntity<Unit> {
        val accessToken = tokenExtractor.getAccessToken()
        val refreshToken = tokenExtractor.getRefreshToken()

        val authToken: AuthTokenDto = authService.reissueToken(accessToken, refreshToken)

        return setAuthTokenHeaders(authToken)
    }

    @DeleteMapping("/logout")
    fun logout(): ResponseEntity<Response<SessionInfoResponse>> {
        val accessToken = tokenExtractor.getAccessToken()
        val sessionInfo = authService.logout(accessToken)

        val data =
            SessionInfoResponse(
                deviceId = sessionInfo.deviceId,
                loginDateTime = sessionInfo.loginDateTime,
                lastActivityDateTime = sessionInfo.lastActivityDateTime,
            )

        return deleteAuthTokenHeaders(
            message = "로그아웃 되었습니다.(${sessionInfo.deviceId})",
            data = data,
        )
    }

    @DeleteMapping("/logout-device")
    fun logout(
        @Valid @RequestBody
        request: LogoutRequest,
    ): ResponseEntity<Response<SessionInfoResponse>> {
        val accessToken = tokenExtractor.getAccessToken()
        val sessionInfo = authService.logout(accessToken, request.deviceId)

        val data =
            SessionInfoResponse(
                deviceId = sessionInfo.deviceId,
                loginDateTime = sessionInfo.loginDateTime,
                lastActivityDateTime = sessionInfo.lastActivityDateTime,
            )

        return deleteAuthTokenHeaders(
            message = "로그아웃 되었습니다.(${sessionInfo.deviceId})",
            data = data,
        )
    }
}

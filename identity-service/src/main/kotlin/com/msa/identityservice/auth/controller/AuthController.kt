package com.msa.identityservice.auth.controller

import com.msa.identityservice.annotation.LoginUser
import com.msa.identityservice.auth.controller.request.LoginRequest
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
    ): ResponseEntity<Void> {
        val loginRequestInfo = request.toLoginRequestInfo()
        val loginSettingToken = authService.login(loginRequestInfo)

        return setTokenHeaders(loginSettingToken)
    }

    private fun setTokenHeaders(loginAuthToken: LoginAuthToken): ResponseEntity<Void> {
        val (accessTokenHeader, refreshTokenCookie) = loginAuthToken
        val responseCookie: ResponseCookie = ResponseCookie.from(refreshTokenCookie.name, refreshTokenCookie.value)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(refreshTokenCookie.refreshDuration)
            .build()

        return ResponseEntity.noContent()
            .header(accessTokenHeader.name, accessTokenHeader.value)
            .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
            .header("Access-Control-Expose-Headers", accessTokenHeader.name)
            .build()
    }

    @GetMapping("/refresh")
    fun tokenReissue(): ResponseEntity<Void> {
        val loginSettingToken = authService.tokenReissue()

        return setTokenHeaders(loginSettingToken)
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

}

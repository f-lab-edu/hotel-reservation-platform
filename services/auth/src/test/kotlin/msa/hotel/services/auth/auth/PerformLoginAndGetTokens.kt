package msa.hotel.services.auth.auth

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import msa.hotel.services.auth.application.identity.command.RegisterIdentityCommand
import msa.hotel.services.auth.infrastructure.web.auth.dto.LoginRequest
import msa.hotel.services.auth.infrastructure.web.auth.header.HeaderConstants.T_ACCESS_HEADER_NAME
import msa.hotel.services.auth.infrastructure.web.auth.header.HeaderConstants.T_REFRESH_COOKIE_NAME
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

fun performLoginAndGetTokens(
    request: LoginRequest,
    mockMvc: MockMvc,
    om: ObjectMapper,
): Pair<String, Cookie> {
    val result =
        mockMvc
            .post("/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = om.writeValueAsString(request)
            }.andReturn()

    val accessToken = result.response.getHeader(T_ACCESS_HEADER_NAME)!!.substring(7)
    val refreshTokenCookie = result.response.getCookie(T_REFRESH_COOKIE_NAME)!!
    return Pair(accessToken, refreshTokenCookie)
}

fun createLoginRequest(
    command: RegisterIdentityCommand,
    deviceId: String,
) = LoginRequest(
    email = command.email,
    password = command.password,
    role = command.role,
    deviceId = deviceId,
)

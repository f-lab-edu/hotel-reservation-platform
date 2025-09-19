package msa.hotel.services.auth.infrastructure.web.auth

import jakarta.validation.Valid
import msa.hotel.services.auth.application.auth.AuthService
import msa.hotel.services.auth.application.auth.dto.AuthTokenDto
import msa.hotel.services.auth.infrastructure.web.auth.dto.LoginRequest
import msa.hotel.services.auth.infrastructure.web.auth.header.RequestHeaderTokenExtractor
import msa.hotel.services.auth.infrastructure.web.auth.header.setAuthTokenHeaders
import org.springframework.http.ResponseEntity
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
}

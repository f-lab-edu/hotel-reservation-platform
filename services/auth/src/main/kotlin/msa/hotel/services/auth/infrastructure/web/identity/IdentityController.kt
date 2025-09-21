package msa.hotel.services.auth.infrastructure.web.identity

import jakarta.validation.Valid
import msa.hotel.modules.web.response.Response
import msa.hotel.services.auth.application.identity.IdentityService
import msa.hotel.services.auth.application.identity.dto.IdentityDto
import msa.hotel.services.auth.domain.identity.policy.EmailVerifyPolicy
import msa.hotel.services.auth.infrastructure.web.identity.dto.EmailConfirmRequest
import msa.hotel.services.auth.infrastructure.web.identity.dto.EmailConfirmResponse
import msa.hotel.services.auth.infrastructure.web.identity.dto.RegisterIdentityRequest
import msa.hotel.services.auth.infrastructure.web.identity.dto.RegisterIdentityResponse
import msa.hotel.services.auth.infrastructure.web.identity.dto.SendVerifyEmailRequest
import msa.hotel.services.auth.infrastructure.web.identity.dto.SendVerifyEmailResponse
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/identity")
class IdentityController(
    private val identityService: IdentityService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid
        @RequestBody
        request: RegisterIdentityRequest,
    ): Response<RegisterIdentityResponse> {
        val command = request.toRegisterCommand()
        val identity: IdentityDto = identityService.register(command)

        val data = RegisterIdentityResponse.from(identity)

        return Response.create(
            message = "Identity 정보를 등록 했습니다.",
            data = data,
        )
    }

    @PostMapping("/verify/request")
    fun sendVerifyEmail(
        @Valid
        @RequestBody
        request: SendVerifyEmailRequest,
    ): Response<SendVerifyEmailResponse> {
        val command = request.toSendVerifyEmailCommand()
        val identity: IdentityDto = identityService.sendVerifyEmail(command)

        val data = SendVerifyEmailResponse.from(identity)

        return Response.create(
            message = "${identity.email} 메일 발송 처리 됐습니다.",
            data = data,
        )
    }

    @GetMapping("/verify/confirm")
    fun confirmEmail(
        @Validated
        request: EmailConfirmRequest,
    ): String {
        val command = request.toEmailConfirmCommand()
        val identity: IdentityDto = identityService.confirmEmail(command)

        val data = EmailConfirmResponse.from(identity)

        // 별도 View Client 부재로 간단한 알림창으로 응답 구현
        return EmailVerifyPolicy.confirmResponse(data.email)
    }
}

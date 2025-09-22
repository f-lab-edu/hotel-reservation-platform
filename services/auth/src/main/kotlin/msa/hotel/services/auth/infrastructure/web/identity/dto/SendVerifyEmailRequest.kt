package msa.hotel.services.auth.infrastructure.web.identity.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import msa.hotel.services.auth.application.identity.command.SendVerifyEmailCommand

data class SendVerifyEmailRequest(
    @field:NotBlank(message = "이메일 등록은 필수 입니다.")
    @field:Email(message = "이메일 형식이 유효하지 않습니다.")
    val email: String?,
) {
    fun toSendVerifyEmailCommand(): SendVerifyEmailCommand =
        SendVerifyEmailCommand(
            email = email!!,
        )
}

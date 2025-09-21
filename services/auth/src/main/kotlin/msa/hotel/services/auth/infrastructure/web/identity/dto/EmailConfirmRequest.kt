package msa.hotel.services.auth.infrastructure.web.identity.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import msa.hotel.services.auth.application.identity.command.EmailConfirmCommand

@Suppress("ktlint:standard:no-blank-line-in-list")
data class EmailConfirmRequest(
    @field:NotBlank(message = "이메일 등록은 필수 입니다.")
    @field:Email(message = "이메일 형식이 유효하지 않습니다.")
    val email: String?,

    @field:NotBlank(message = "검증 코드는 필수 입니다.")
    val code: String?,
) {
    fun toEmailConfirmCommand(): EmailConfirmCommand =
        EmailConfirmCommand(
            email = email!!,
            code = code!!,
        )
}

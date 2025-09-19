package msa.hotel.services.auth.infrastructure.web.identity.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import msa.hotel.services.auth.application.identity.command.RegisterIdentityCommand
import msa.hotel.services.auth.domain.identity.model.Role
import msa.hotel.services.auth.infrastructure.web.validation.StrongPassword

@Suppress("ktlint:standard:no-blank-line-in-list")
data class RegisterIdentityRequest(
    @field:NotBlank(message = "이메일 등록은 필수 입니다.")
    @field:Email(message = "이메일 형식이 유효하지 않습니다.")
    val email: String?,

    @field:StrongPassword
    val password: String?,

    @field:NotNull(message = "Role 값이 유효하지 않습니다. (MEMBER, HOST, ADMIN)")
    val role: Role?,
) {
    fun toRegisterCommand(): RegisterIdentityCommand =
        RegisterIdentityCommand(
            email = email!!,
            password = password!!,
            role = role!!,
        )
}

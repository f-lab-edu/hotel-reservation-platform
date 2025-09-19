package msa.hotel.services.auth.infrastructure.web.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import msa.hotel.services.auth.application.auth.command.LoginCommand
import msa.hotel.services.auth.domain.identity.model.Role

@Suppress("ktlint:standard:no-blank-line-in-list")
data class LoginRequest(
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다.")
    val email: String?,

    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    val password: String?,

    @field:NotNull(message = "Role 값이 유효하지 않습니다. (MEMBER, HOST, ADMIN)")
    val role: Role?,

    @field:NotBlank(message = "로그인 기기 ID는 필수입니다.")
    val deviceId: String?, // 추후 deviceId -> Fingerprinting
) {
    fun toLoginCommand() =
        LoginCommand(
            email = email!!,
            password = password!!,
            role = role!!,
            deviceId = deviceId!!,
        )
}

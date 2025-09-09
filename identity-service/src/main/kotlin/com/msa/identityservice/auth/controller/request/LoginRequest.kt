package com.msa.identityservice.auth.controller.request

import com.msa.identityservice.auth.service.dto.LoginRequestInfo
import com.msa.identityservice.auth.token.enums.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


data class LoginRequest(
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다.")
    val email: String?,

    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    val password: String?,

    @field:NotNull(message = "Role은 MEMBER, HOST, ADMIN 중 하나여야 합니다.")
    val role: Role?,

    @field:NotBlank(message = "로그인 기기 ID는 필수입니다.")
    val deviceId: String? // TODO: Device Fingerprinting
) {
    fun toLoginRequestInfo() = LoginRequestInfo(
        email = email!!,
        password = password!!,
        role = role!!,
        deviceId = deviceId!!
    )

}

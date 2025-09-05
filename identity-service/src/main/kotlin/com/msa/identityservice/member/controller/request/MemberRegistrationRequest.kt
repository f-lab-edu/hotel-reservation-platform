package com.msa.identityservice.member.controller.request

import com.msa.identityservice.member.service.dto.RegisterMemberDto
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class MemberRegistrationRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Pattern(regexp = PASSWORD_PATTERN,message = "비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
    val password: String,

    @field:NotBlank(message = "휴대폰 번호는 필수입니다.")
    @field:Pattern(regexp = PHONE_NUMBER_PATTERN, message = "올바른 휴대폰 번호 형식이 아닙니다. (010-XXXX-XXXX)")
    val phoneNumber: String
) {
    fun toRegisterMemberDto(): RegisterMemberDto {
        return RegisterMemberDto(
            email = this.email,
            password = this.password,
            phoneNumber = this.phoneNumber
        )
    }

    companion object {
        // 비밀번호 정책 검증 정규식 (최소 8자, 영문, 숫자, 특수문자 각 1개 이상 포함)
        private const val PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$"

        // 대한민국 휴대폰 번호 형식 검증 정규식 (010-XXXX-XXXX)
        private const val PHONE_NUMBER_PATTERN = "^010-\\d{4}-\\d{4}$"
    }
}

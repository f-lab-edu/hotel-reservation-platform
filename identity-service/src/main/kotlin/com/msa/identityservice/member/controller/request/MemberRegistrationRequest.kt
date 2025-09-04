package com.msa.identityservice.member.controller.request

import com.msa.identityservice.member.service.dto.RegisterMemberDto

data class MemberRegistrationRequest(
    val email: String,
    val password: String,
    val phoneNumber: String
) {
    fun validateToRegisterMemberDto(): RegisterMemberDto {
        require(email.matches(EMAIL_REGEX)) { "올바른 이메일 형식이 아닙니다." }
        require(password.matches(PASSWORD_REGEX)) { "비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다." }
        require(phoneNumber.matches(PHONE_NUMBER_REGEX)) { "올바른 휴대폰 번호 형식이 아닙니다. (010-XXXX-XXXX)" }

        return RegisterMemberDto(
            email = this.email,
            password = this.password,
            phoneNumber = this.phoneNumber
        )
    }

    companion object {
        // 이메일 형식 검증 정규식
        private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")

        // 비밀번호 정책 검증 정규식 (최소 8자, 영문, 숫자, 특수문자 각 1개 이상 포함)
        private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")

        // 대한민국 휴대폰 번호 형식 검증 정규식 (010-XXXX-XXXX)
        private val PHONE_NUMBER_REGEX = Regex("^010-\\d{4}-\\d{4}$")
    }
}

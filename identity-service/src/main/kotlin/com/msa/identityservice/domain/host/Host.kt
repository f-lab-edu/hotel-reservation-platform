package com.msa.identityservice.domain.host

import com.msa.supportmodule.exception.BusinessErrorCode

data class Host(
    val id: Long,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val status: HostStatus
) {

    init {
        if (email.isBlank()) {
            throw BusinessErrorCode.CONFLICT.exception("이메일은 빈칸일 수 없습니다.")
        }
        if (phoneNumber.isBlank()) {
            throw BusinessErrorCode.CONFLICT.exception("전화번호는 빈칸일 수 없습니다.")
        }
        if (password.isBlank()) {
            throw BusinessErrorCode.CONFLICT.exception("비밀번호는 빈칸일 수 없습니다.")
        }
    }

}

package com.msa.identityservice.member.service.dto

import com.msa.identityservice.domain.member.Member
import com.msa.identityservice.domain.member.MemberStatus


data class RegisterMemberDto(
    val email: String,
    val password: String,
    val phoneNumber: String
) {

    fun toNewMember(newId: Long, encoderPassword: String): Member {
        return Member(
            id = newId,
            email = email.lowercase(),
            password = encoderPassword,
            phoneNumber = phoneNumber,
            status = MemberStatus.ACTIVE
        )
    }

}

package com.msa.identityservice.member.service.dto

import com.msa.identityservice.jooq.tables.pojos.Member


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
            phoneNumber = phoneNumber
        )
    }

}

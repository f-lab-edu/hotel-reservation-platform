package com.msa.identityservice.host.service.dto

import com.msa.identityservice.domain.host.Host
import com.msa.identityservice.domain.host.HostStatus


data class RegisterHostDto(
    val email: String,
    val password: String,
    val phoneNumber: String
) {

    fun toNewHost(newId: Long, encoderPassword: String): Host {
        return Host(
            id = newId,
            email = email.lowercase(),
            password = encoderPassword,
            phoneNumber = phoneNumber,
            status = HostStatus.ACTIVE
        )
    }

}

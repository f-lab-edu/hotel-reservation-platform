package com.msa.identityservice.auth.service.dto

import com.msa.supportmodule.auth.token.enums.Role

data class LoginRequestInfo(
    val email: String,
    val password: String,
    val role: Role,
    val deviceId: String
)

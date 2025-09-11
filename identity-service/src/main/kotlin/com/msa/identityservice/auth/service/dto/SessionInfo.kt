package com.msa.identityservice.auth.service.dto

import java.time.LocalDateTime


data class SessionInfo(
    val deviceId: String,
    val loginDateTime: LocalDateTime,
    val lastActivityDateTime: LocalDateTime,
)

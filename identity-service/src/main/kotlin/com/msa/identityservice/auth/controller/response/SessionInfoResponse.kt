package com.msa.identityservice.auth.controller.response

import java.time.LocalDateTime


data class SessionInfoResponse(
    val deviceId: String,
    val loginDateTime: LocalDateTime,
    val lastActivityDateTime: LocalDateTime,
)

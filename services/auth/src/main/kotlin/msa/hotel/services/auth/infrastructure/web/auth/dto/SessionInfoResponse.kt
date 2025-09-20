package msa.hotel.services.auth.infrastructure.web.auth.dto

import java.time.LocalDateTime

data class SessionInfoResponse(
    val deviceId: String,
    val loginDateTime: LocalDateTime,
    val lastActivityDateTime: LocalDateTime,
)

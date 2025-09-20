package msa.hotel.services.auth.application.auth.dto

import java.time.LocalDateTime

data class SessionInfoDto(
    val deviceId: String,
    val loginDateTime: LocalDateTime,
    val lastActivityDateTime: LocalDateTime,
)

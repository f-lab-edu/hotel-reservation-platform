package msa.hotel.services.auth.infrastructure.web.auth.dto

data class LogoutAllResponse(
    val logoutInfos: List<SessionInfoResponse>,
)

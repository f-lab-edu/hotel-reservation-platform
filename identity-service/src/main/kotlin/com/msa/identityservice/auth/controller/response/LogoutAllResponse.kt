package com.msa.identityservice.auth.controller.response


data class LogoutAllResponse(
    val logoutInfos: List<SessionInfoResponse>
)

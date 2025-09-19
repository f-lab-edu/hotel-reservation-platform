package msa.hotel.services.auth.application.auth.command

import msa.hotel.services.auth.domain.identity.model.Role

data class LoginCommand(
    val email: String,
    val password: String,
    val role: Role,
    val deviceId: String,
)

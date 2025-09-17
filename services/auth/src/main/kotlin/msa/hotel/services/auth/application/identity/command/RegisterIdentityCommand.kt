package msa.hotel.services.auth.application.identity.command

import msa.hotel.services.auth.domain.identity.model.Role

data class RegisterIdentityCommand(
    val email: String,
    val password: String,
    val role: Role,
)

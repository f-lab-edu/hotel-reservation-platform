package msa.hotel.services.auth.domain.identity.model.event

import java.util.UUID

data class IdentityCreated(
    val id: UUID,
    val username: String,
    val password: String,
    val email: String,
    val roleKey: String,
)

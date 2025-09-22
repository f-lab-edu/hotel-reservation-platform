package msa.hotel.services.auth.infrastructure.web.identity.dto

import msa.hotel.services.auth.application.identity.dto.IdentityDto

data class PasswordChangeResponse(
    val id: ULong,
    val email: String,
    val role: String,
    val createdAt: String,
) {
    companion object {
        fun from(identity: IdentityDto): PasswordChangeResponse =
            PasswordChangeResponse(
                id = identity.id.value,
                email = identity.email,
                role = identity.role.name,
                createdAt = identity.createdAt.toString(),
            )
    }
}

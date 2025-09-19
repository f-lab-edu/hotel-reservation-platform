package msa.hotel.services.auth.infrastructure.web.identity.dto

import msa.hotel.services.auth.application.identity.dto.IdentityDto

data class RegisterIdentityResponse(
    val id: ULong,
    val email: String,
    val role: String,
    val createdAt: String,
) {
    companion object {
        fun from(identity: IdentityDto): RegisterIdentityResponse =
            RegisterIdentityResponse(
                id = identity.id.value,
                email = identity.email,
                role = identity.role.name,
                createdAt = identity.createdAt.toString(),
            )
    }
}

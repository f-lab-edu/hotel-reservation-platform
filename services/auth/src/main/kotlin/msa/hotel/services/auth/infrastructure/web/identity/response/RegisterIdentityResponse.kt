package msa.hotel.services.auth.infrastructure.web.identity.response

import msa.hotel.services.auth.application.identity.dto.IdentityDto

data class RegisterIdentityResponse(
    val id: ULong,
    val email: String,
    val role: String,
    val createdAt: String,
) {
    companion object {
        fun from(dto: IdentityDto): RegisterIdentityResponse =
            RegisterIdentityResponse(
                id = dto.id.value,
                email = dto.email,
                role = dto.role.name,
                createdAt = dto.createdAt.toString(),
            )
    }
}

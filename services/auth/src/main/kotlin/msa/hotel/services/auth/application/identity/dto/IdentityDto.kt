package msa.hotel.services.auth.application.identity.dto

import msa.hotel.services.auth.domain.identity.model.Identity
import msa.hotel.services.auth.domain.identity.model.IdentityId
import msa.hotel.services.auth.domain.identity.model.Role
import msa.hotel.services.auth.domain.identity.model.Status
import java.time.Instant

data class IdentityDto(
    val id: IdentityId,
    val email: String,
    val role: Role,
    val status: Status,
    val failedLoginCount: UInt,
    val lockedUntil: Instant?,
    val passwordUpdatedAt: Instant,
    val createdAt: Instant,
) {
    companion object {
        fun from(identity: Identity): IdentityDto =
            IdentityDto(
                id = identity.id,
                email = identity.email,
                role = identity.role,
                status = identity.status,
                failedLoginCount = identity.failedLoginCount,
                lockedUntil = identity.lockedUntil,
                passwordUpdatedAt = identity.passwordUpdatedAt,
                createdAt = identity.createdAt,
            )
    }
}

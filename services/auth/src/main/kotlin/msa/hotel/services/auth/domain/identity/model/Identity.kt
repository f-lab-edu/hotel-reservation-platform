package msa.hotel.services.auth.domain.identity.model

import java.time.Instant

class Identity(
    val id: IdentityId,
    val email: String,
    val passwordHash: String,
    val role: Role,
    status: Status = Status.PENDING,
    val emailVerifiedAt: Instant? = null,
    val failedLoginCount: UInt = 0u,
    val lockedUntil: Instant? = null,
    val passwordUpdatedAt: Instant,
    val deletedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    var status: Status = status
        private set

    fun activate() {
        check(status == Status.PENDING) { "Only PENDING can be activated" }
        status = Status.ACTIVE
    }

    fun lock(until: Instant) {
        status = Status.LOCKED
    }

    fun disable() {
        status = Status.DISABLED
    }
}

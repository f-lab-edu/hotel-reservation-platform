package msa.hotel.services.auth.domain.identity.model

import java.time.Instant

class Identity(
    val id: IdentityId,
    val email: String,
    passwordHash: String,
    val role: Role,
    status: Status = Status.ACTIVE,
    failedLoginCount: UInt = 0u,
    lockedUntil: Instant? = null,
    passwordUpdatedAt: Instant,
    deletedAt: Instant? = null,
    val createdAt: Instant,
) {
    var passwordHash = passwordHash
        private set

    var passwordUpdatedAt = passwordUpdatedAt
        private set

    var status: Status = status
        private set

    var failedLoginCount = failedLoginCount
        private set

    var lockedUntil = lockedUntil
        private set

    var deletedAt = deletedAt
        private set

    fun lock(until: Instant) {
        status = Status.LOCKED
        lockedUntil = until
    }

    fun disable() {
        status = Status.DISABLED
        deletedAt = Instant.now()
    }

    fun incrementFailedLoginCount() {
        failedLoginCount++
    }

    fun unLock() {
        lockedUntil = null
        failedLoginCount = 0u
        status = Status.ACTIVE
    }
}

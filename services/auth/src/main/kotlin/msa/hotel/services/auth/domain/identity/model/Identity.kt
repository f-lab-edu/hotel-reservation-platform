package msa.hotel.services.auth.domain.identity.model

import msa.hotel.modules.web.exception.ErrorCode
import java.time.Instant

class Identity(
    val id: IdentityId,
    val email: String,
    passwordHash: String,
    val role: Role,
    status: Status = Status.PENDING,
    mailVerificationAt: Instant? = null,
    failedLoginCount: UInt = 0u,
    lockedUntil: Instant? = null,
    passwordUpdatedAt: Instant,
    lastLoginAt: Instant? = null,
    deletedAt: Instant? = null,
    val createdAt: Instant,
) {
    init {
        if (email.isBlank()) {
            throw ErrorCode.CONFLICT.exception("email is blank")
        }
        if (!email.matches(".+@.+\\..+".toRegex())) {
            throw ErrorCode.CONFLICT.exception("invalid email format")
        }
        if (passwordHash.isBlank()) {
            throw ErrorCode.CONFLICT.exception("password hash is blank")
        }
        if (mailVerificationAt != null && mailVerificationAt.isBefore(createdAt)) {
            throw ErrorCode.CONFLICT.exception("mail verification at is before created at")
        }
        if (lockedUntil != null && lockedUntil.isBefore(createdAt)) {
            throw ErrorCode.CONFLICT.exception("locked until is before created at")
        }
        if (lastLoginAt != null && lastLoginAt.isBefore(createdAt)) {
            throw ErrorCode.CONFLICT.exception("last login at is after created at")
        }
        if (deletedAt != null && deletedAt.isBefore(createdAt)) {
            throw ErrorCode.CONFLICT.exception("deleted at is before created at")
        }
    }

    var passwordHash = passwordHash
        private set

    var passwordUpdatedAt = passwordUpdatedAt
        private set

    var status: Status = status
        private set

    var mailVerificationAt = mailVerificationAt
        private set

    var failedLoginCount = failedLoginCount
        private set

    var lockedUntil = lockedUntil
        private set

    var deletedAt = deletedAt
        private set

    var lastLoginAt = lastLoginAt
        private set

    fun verifyMail() {
        status = Status.ACTIVE
        mailVerificationAt = Instant.now()
    }

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

    fun loginSuccess(loginAt: Instant = Instant.now()) {
        failedLoginCount = 0u
        lastLoginAt = loginAt
    }

    fun updatePassword(passwordHash: String) {
        this.passwordHash = passwordHash
        this.passwordUpdatedAt = Instant.now()
    }
}

package msa.hotel.services.auth.infrastructure.persistence.jooq.mapper

import msa.hotel.services.auth.domain.identity.model.Identity
import msa.hotel.services.auth.domain.identity.model.IdentityId
import msa.hotel.services.auth.domain.identity.model.Role
import msa.hotel.services.auth.domain.identity.model.Status
import msa.hotel.services.auth.jooq.tables.records.IdentitiesRecord
import org.jooq.types.UInteger
import org.jooq.types.ULong
import java.time.LocalDateTime
import java.time.ZoneOffset

internal fun IdentitiesRecord.toDomain(): Identity =
    Identity(
        id = IdentityId(this.id.toLong().toULong()),
        email = this.email,
        passwordHash = this.passwordHash,
        role = Role.valueOf(this.role),
        status = Status.valueOf(requireNotNull(this.status)),
        mailVerificationAt = this.mailVerificationAt?.toInstant(ZoneOffset.UTC),
        lastLoginAt = this.lastLoginAt?.toInstant(ZoneOffset.UTC),
        failedLoginCount = (this.failedLoginCount ?: UInteger.valueOf(0)).toInt().toUInt(),
        lockedUntil = this.lockedUntil?.toInstant(ZoneOffset.UTC),
        passwordUpdatedAt = requireNotNull(this.passwordUpdatedAt).toInstant(ZoneOffset.UTC),
        deletedAt = this.deletedAt?.toInstant(ZoneOffset.UTC),
        createdAt = requireNotNull(this.createdAt).toInstant(ZoneOffset.UTC),
    )

internal fun Identity.toRecord(): IdentitiesRecord =
    IdentitiesRecord(
        id = ULong.valueOf(this.id.value.toLong()),
        email = this.email,
        passwordHash = this.passwordHash,
        role = this.role.name,
        status = this.status.name,
        mailVerificationAt = this.mailVerificationAt?.atZone(ZoneOffset.UTC)?.toLocalDateTime(),
        failedLoginCount = UInteger.valueOf(this.failedLoginCount.toInt()),
        lockedUntil = this.lockedUntil?.atZone(ZoneOffset.UTC)?.toLocalDateTime(),
        passwordUpdatedAt = this.passwordUpdatedAt.atZone(ZoneOffset.UTC).toLocalDateTime(),
        lastLoginAt = this.lastLoginAt?.atZone(ZoneOffset.UTC)?.toLocalDateTime(),
        deletedAt = this.deletedAt?.atZone(ZoneOffset.UTC)?.toLocalDateTime(),
        createdAt = this.createdAt.atZone(ZoneOffset.UTC).toLocalDateTime(),
        updatedAt = LocalDateTime.now(),
    )

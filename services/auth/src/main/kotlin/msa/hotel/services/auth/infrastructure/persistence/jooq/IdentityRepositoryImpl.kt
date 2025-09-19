package msa.hotel.services.auth.infrastructure.persistence.jooq

import msa.hotel.services.auth.domain.identity.model.Identity
import msa.hotel.services.auth.domain.identity.model.IdentityId
import msa.hotel.services.auth.domain.identity.model.Status
import msa.hotel.services.auth.domain.identity.port.IdentityRepository
import msa.hotel.services.auth.infrastructure.persistence.jooq.mapper.toDomain
import msa.hotel.services.auth.infrastructure.persistence.jooq.mapper.toRecord
import msa.hotel.services.auth.jooq.tables.references.IDENTITIES
import org.jooq.DSLContext
import org.jooq.types.ULong
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class IdentityRepositoryImpl(
    private val dsl: DSLContext,
) : IdentityRepository {
    override fun save(identity: Identity): Identity {
        val record = identity.toRecord()
        val values = record.intoMap().toMutableMap()
        values.remove(IDENTITIES.IS_ACTIVE.name)

        dsl
            .insertInto(IDENTITIES)
            .set(values)
            .onDuplicateKeyUpdate()
            .set(values)
            .execute()

        return identity
    }

    override fun findActiveByEmail(email: String): Identity? =
        dsl
            .selectFrom(IDENTITIES)
            .where(IDENTITIES.EMAIL.eq(email))
            .and(IDENTITIES.IS_ACTIVE.isTrue)
            .fetchOne()
            ?.toDomain()

    override fun findById(id: IdentityId): Identity? =
        dsl
            .selectFrom(IDENTITIES)
            .where(IDENTITIES.ID.eq(ULong.valueOf(id.value.toLong())))
            .fetchOne()
            ?.toDomain()

    override fun disable(id: IdentityId): Boolean =
        dsl
            .update(IDENTITIES)
            .set(IDENTITIES.STATUS, Status.DISABLED.name)
            .set(IDENTITIES.DELETED_AT, LocalDateTime.now())
            .where(IDENTITIES.ID.eq(ULong.valueOf(id.value.toLong())))
            .execute() > 0
}

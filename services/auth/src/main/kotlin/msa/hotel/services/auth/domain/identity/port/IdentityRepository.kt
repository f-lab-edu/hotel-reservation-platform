package msa.hotel.services.auth.domain.identity.port

import msa.hotel.services.auth.domain.identity.model.Identity
import msa.hotel.services.auth.domain.identity.model.IdentityId

interface IdentityRepository {
    fun save(identity: Identity): Identity

    fun findActiveByEmail(email: String): Identity?

    fun findById(id: IdentityId): Identity?

    fun disable(id: IdentityId): Boolean
}

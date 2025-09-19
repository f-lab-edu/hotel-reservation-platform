package msa.hotel.services.auth.infrastructure.persistence.redis.key

object AuthTokenKey {
    private const val REFRESH_TOKEN_PREFIX = "refresh_token"
    private const val ACTIVE_JTI_KEY_PREFIX = "active_jti"
    private const val SESSIONS_AGES_PREFIX = "sessions_ages"

    fun makeRefreshTokenKey(
        role: String,
        userId: ULong,
    ) = "${REFRESH_TOKEN_PREFIX}:${role.lowercase()}:$userId"

    fun makeActiveJtiKey(jti: String) = "${ACTIVE_JTI_KEY_PREFIX}:$jti"

    fun makeSessionKey(
        role: String,
        userId: ULong,
    ) = "${SESSIONS_AGES_PREFIX}:${role.lowercase()}:$userId"
}

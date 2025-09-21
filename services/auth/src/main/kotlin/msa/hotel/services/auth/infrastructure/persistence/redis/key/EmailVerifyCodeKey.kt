package msa.hotel.services.auth.infrastructure.persistence.redis.key

object EmailVerifyCodeKey {
    private const val VERIFY_CODE_PREFIX = "email-verify"

    fun makeEmailVerifyCodeKey(userId: ULong) = "${VERIFY_CODE_PREFIX}:$userId"
}

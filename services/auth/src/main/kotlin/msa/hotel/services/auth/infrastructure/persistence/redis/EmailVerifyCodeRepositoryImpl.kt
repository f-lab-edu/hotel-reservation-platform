package msa.hotel.services.auth.infrastructure.persistence.redis

import msa.hotel.services.auth.domain.identity.policy.EmailVerifyPolicy.VERIFY_EMAIL_EXPIRATION_IN_HOURS
import msa.hotel.services.auth.domain.identity.port.EmailVerifyCodeRepository
import msa.hotel.services.auth.infrastructure.persistence.redis.key.EmailVerifyCodeKey.makeEmailVerifyCodeKey
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class EmailVerifyCodeRepositoryImpl(
    private val rt: RedisTemplate<String, String>,
) : EmailVerifyCodeRepository {
    override fun saveCode(
        userId: ULong,
        code: String,
    ) {
        val key = makeEmailVerifyCodeKey(userId)

        rt.opsForValue().set(
            key,
            code,
            VERIFY_EMAIL_EXPIRATION_IN_HOURS,
            TimeUnit.HOURS,
        )
    }

    override fun findCodeByUserId(userId: ULong): String? {
        val key = makeEmailVerifyCodeKey(userId)

        return rt.opsForValue().get(key)
    }

    override fun deleteCodeByUserId(userId: ULong) {
        val key = makeEmailVerifyCodeKey(userId)

        rt.delete(key)
    }
}

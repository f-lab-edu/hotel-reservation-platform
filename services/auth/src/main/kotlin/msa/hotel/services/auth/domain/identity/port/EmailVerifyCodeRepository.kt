package msa.hotel.services.auth.domain.identity.port

interface EmailVerifyCodeRepository {
    fun saveCode(
        userId: ULong,
        code: String,
    )

    fun findCodeByUserId(userId: ULong): String?

    fun deleteCodeByUserId(userId: ULong)
}

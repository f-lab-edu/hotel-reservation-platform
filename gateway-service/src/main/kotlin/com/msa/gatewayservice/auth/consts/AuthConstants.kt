package com.msa.gatewayservice.auth.consts


object AuthConstants {
    const val AUTH_HEADER_PREFIX = "Bearer "
    const val CONTEXT_NOT_FOUND_MESSAGE = "인증 정보가 존재하지 않습니다."


    private const val ACTIVE_JTI_KEY_PREFIX = "active_jti"

    fun getActiveJtiKey(jti: String) = "$ACTIVE_JTI_KEY_PREFIX:$jti"
}

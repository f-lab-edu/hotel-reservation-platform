package com.msa.identityservice.auth.consts

import com.msa.identityservice.auth.token.enums.Role

object AuthConstants {
    const val REFRESH_COOKIE_NAME = "refreshToken"
    const val AUTH_HEADER_NAME = "Authorization"
    const val AUTH_HEADER_PREFIX = "Bearer "
    const val CONTEXT_NOT_FOUND_MESSAGE = "인증 정보가 존재하지 않습니다."

    const val ASIA_TIME_ZONE = "Asia/Seoul"

    private const val SESSIONS_AGES_PREFIX = "sessions_ages"
    private const val ACTIVE_JTI_KEY_PREFIX = "active_jti"
    private const val REFRESH_TOKEN_PREFIX = "refresh_token"

    fun getAccessTokenHeaderValue(token: String) = "$AUTH_HEADER_PREFIX$token"
    fun getRefreshTokenKey(role: Role, userId: Long) = "$REFRESH_TOKEN_PREFIX:${role.name.lowercase()}:$userId"
    fun getActiveJtiKey(jti: String) = "$ACTIVE_JTI_KEY_PREFIX:$jti"
    fun getSessionKey(role: Role, userId: Long) = "$SESSIONS_AGES_PREFIX:${role.name.lowercase()}:${userId}"
}

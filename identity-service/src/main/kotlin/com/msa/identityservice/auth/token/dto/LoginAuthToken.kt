package com.msa.identityservice.auth.token.dto

data class LoginAuthToken(
    val accessTokenHeader: AccessTokenHeader,
    val refreshTokenCookie: RefreshTokenCookie
)

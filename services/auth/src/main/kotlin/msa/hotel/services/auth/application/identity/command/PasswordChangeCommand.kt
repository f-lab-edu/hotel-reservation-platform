package msa.hotel.services.auth.application.identity.command

import msa.hotel.modules.jwt.token.dto.TokenAuthInfo

data class PasswordChangeCommand(
    val authInfo: TokenAuthInfo,
    val currentPassword: String,
    val changePassword: String,
)

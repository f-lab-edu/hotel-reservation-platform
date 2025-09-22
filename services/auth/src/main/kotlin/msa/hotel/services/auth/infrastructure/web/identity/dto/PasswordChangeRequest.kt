package msa.hotel.services.auth.infrastructure.web.identity.dto

import msa.hotel.modules.jwt.token.dto.TokenAuthInfo
import msa.hotel.services.auth.application.identity.command.PasswordChangeCommand
import msa.hotel.services.auth.infrastructure.web.annotations.validation.StrongPassword

@Suppress("ktlint:standard:no-blank-line-in-list")
data class PasswordChangeRequest(
    @field:StrongPassword
    val currentPassword: String?,

    @field:StrongPassword
    val changePassword: String?,
) {
    fun toPasswordChangeCommand(authInfo: TokenAuthInfo): PasswordChangeCommand =
        PasswordChangeCommand(
            authInfo = authInfo,
            currentPassword = currentPassword!!,
            changePassword = changePassword!!,
        )
}

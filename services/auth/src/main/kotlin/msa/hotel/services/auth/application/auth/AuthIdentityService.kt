package msa.hotel.services.auth.application.auth

import msa.hotel.services.auth.application.auth.command.LoginCommand
import msa.hotel.services.auth.domain.identity.model.Identity
import java.time.Instant

interface AuthIdentityService {
    fun validateLogin(command: LoginCommand): Identity

    fun updateLoginSuccess(
        identity: Identity,
        loginAt: Instant,
    )
}

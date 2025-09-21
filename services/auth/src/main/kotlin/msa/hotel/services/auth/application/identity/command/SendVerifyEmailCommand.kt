package msa.hotel.services.auth.application.identity.command

data class SendVerifyEmailCommand(
    val email: String,
)

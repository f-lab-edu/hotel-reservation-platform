package msa.hotel.services.auth.application.identity.command

data class EmailConfirmCommand(
    val email: String,
    val code: String,
)

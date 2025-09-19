package msa.hotel.services.auth.domain.identity.policy.dto

data class PasswordValidationResult(
    val isValid: Boolean,
    val errorCause: String? = null,
)

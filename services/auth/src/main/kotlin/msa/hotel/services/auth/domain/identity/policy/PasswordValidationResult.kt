package msa.hotel.services.auth.domain.identity.policy

data class PasswordValidationResult(
    val isValid: Boolean,
    val error: String? = null,
)

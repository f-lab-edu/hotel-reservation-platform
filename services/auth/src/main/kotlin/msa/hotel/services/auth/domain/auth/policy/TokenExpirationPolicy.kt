package msa.hotel.services.auth.domain.auth.policy

object TokenExpirationPolicy {
    const val ACCESS_TOKEN_EXPIRATION_IN_HOURS = 1L
    const val REFRESH_TOKEN_EXPIRATION_IN_HOURS = 24L
}

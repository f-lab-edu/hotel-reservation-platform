package msa.hotel.services.auth.domain.auth.policy

object LoginFailurePolicy {
    const val MAX_ATTEMPT = 5u
    const val LOCK_TIME_IN_SECONDS = 600L
}

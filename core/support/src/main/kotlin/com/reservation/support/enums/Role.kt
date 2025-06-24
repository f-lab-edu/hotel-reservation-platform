package com.reservation.support.enums

import java.util.*

enum class Role(private val authority: String) {
    HOST("ROLE_HOST"),
    CUSTOMER("ROLE_CUSTOMER"),
    ADMIN("ROLE_ADMIN");

    fun authority(): String {
        return authority
    }

    companion object {
        fun fromAuthority(authority: String): Role {
            return Arrays.stream(entries.toTypedArray())
                .filter { r: Role -> r.authority == authority }
                .findFirst()
                .orElseThrow {
                    IllegalArgumentException(
                        "Unknown role: $authority"
                    )
                }
        }
    }
}

package msa.hotel.services.auth.domain.identity.policy

import msa.hotel.services.auth.domain.identity.policy.dto.PasswordValidationResult

object PasswordPolicy {
    private const val MIN_LENGTH = 8
    private val specialCharacters = "@$!%*#?&".toSet()

    fun validate(password: String): PasswordValidationResult {
        if (password.length < MIN_LENGTH) {
            return PasswordValidationResult(false, "비밀번호는 최소 $MIN_LENGTH 자 이상이어야 합니다.")
        }
        if (!password.any { it.isLetter() }) {
            return PasswordValidationResult(false, "영문자를 최소 1개 포함해야 합니다.")
        }
        if (!password.any { it.isDigit() }) {
            return PasswordValidationResult(false, "숫자를 최소 1개 포함해야 합니다.")
        }
        if (!password.any { it in specialCharacters }) {
            return PasswordValidationResult(false, "특수문자(@$!%*#?&)를 최소 1개 포함해야 합니다.")
        }

        return PasswordValidationResult(true)
    }
}

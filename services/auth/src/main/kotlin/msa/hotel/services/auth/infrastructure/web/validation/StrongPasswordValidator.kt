package msa.hotel.services.auth.infrastructure.web.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import msa.hotel.services.auth.domain.identity.policy.PasswordPolicy
import org.springframework.stereotype.Component

@Component
class StrongPasswordValidator : ConstraintValidator<StrongPassword, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value.isNullOrBlank()) {
            return false
        }
        val result = PasswordPolicy.validate(value)
        if (!result.isValid) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(result.error ?: "비밀번호가 유효하지 않습니다").addConstraintViolation()
        }

        return result.isValid
    }
}

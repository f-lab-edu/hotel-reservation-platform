package msa.hotel.services.auth.application.identity

import msa.hotel.modules.idgenerator.IdGenerator
import msa.hotel.modules.web.exception.ErrorCode
import msa.hotel.services.auth.application.identity.command.RegisterIdentityCommand
import msa.hotel.services.auth.application.identity.dto.IdentityDto
import msa.hotel.services.auth.domain.identity.model.Identity
import msa.hotel.services.auth.domain.identity.model.IdentityId
import msa.hotel.services.auth.domain.identity.policy.PasswordPolicy
import msa.hotel.services.auth.domain.identity.port.IdentityRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class IdentityService(
    private val ids: IdGenerator = IdGenerator(),
    private val repo: IdentityRepository,
    private val pwEncoder: PasswordEncoder,
) {
    @Transactional
    fun register(command: RegisterIdentityCommand): IdentityDto {
        val pwValidResult = PasswordPolicy.validate(command.password)
        if (!pwValidResult.isValid) {
            throw ErrorCode.VALIDATION_ERROR.exception(pwValidResult.errorCause ?: "비밀번호 정책 위반")
        }

        val email = command.email.trim().lowercase()

        if (repo.findActiveByEmail(email) != null) {
            throw ErrorCode.CONFLICT.exception("이미 사용 중인 이메일입니다. 다른 이메일로 등록해주세요.")
        }

        val id = IdentityId(ids.generate())
        val passwordHash = pwEncoder.encode(command.password)
        val now = Instant.now()
        val identity =
            Identity(
                id = id,
                email = email,
                passwordHash = passwordHash,
                role = command.role,
                passwordUpdatedAt = now,
                createdAt = now,
            )

        val saved = repo.save(identity)

        return IdentityDto.from(saved)
    }
}

package msa.hotel.services.auth.application.identity

import msa.hotel.modules.web.exception.ErrorCode
import msa.hotel.services.auth.application.auth.AuthIdentityService
import msa.hotel.services.auth.application.auth.command.LoginCommand
import msa.hotel.services.auth.domain.auth.policy.LoginFailurePolicy.LOCK_TIME_IN_SECONDS
import msa.hotel.services.auth.domain.auth.policy.LoginFailurePolicy.MAX_ATTEMPT
import msa.hotel.services.auth.domain.identity.model.Identity
import msa.hotel.services.auth.domain.identity.model.Status
import msa.hotel.services.auth.domain.identity.port.IdentityRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class IdentityLoginService(
    private val repo: IdentityRepository,
    private val pwEncoder: PasswordEncoder,
) : AuthIdentityService {
    override fun validateLogin(command: LoginCommand): Identity {
        val (email, password, role) = command

        val identity = repo.findActiveByEmail(email) ?: throw ErrorCode.NOT_FOUND.exception("$email 가입 정보가 존재하지 않습니다.")

        if (identity.role != role) {
            throw ErrorCode.FORBIDDEN.exception("${identity.role.name} 유저로 등록된 이메일입니다.")
        }
        if (identity.status == Status.PENDING) {
            throw ErrorCode.FORBIDDEN.exception("이메일 인증이 완료된 상태가 아닙니다. 메일함을 확인해주세요.")
        }
        if (identity.status == Status.LOCKED) {
            checkLockedLogin(identity)
        }
        if (identity.passwordHash != pwEncoder.encode(password)) {
            checkFailLoginCount(identity)
        }

        return identity
    }

    override fun updateLoginSuccess(
        identity: Identity,
        loginAt: Instant,
    ) {
        identity.loginSuccess(loginAt)
        repo.save(identity)
    }

    private fun checkLockedLogin(identity: Identity) {
        val now = Instant.now()
        val lockedUntil: Instant? = identity.lockedUntil

        if (lockedUntil == null || lockedUntil.isAfter(now)) {
            identity.unLock()
            repo.save(identity)
            return
        }

        if (lockedUntil.isBefore(now)) {
            val lockedDuration = Duration.between(identity.lockedUntil, Instant.now()).toSeconds()
            throw ErrorCode.UNAUTHORIZED.exception("계정이 잠긴 상태입니다. 남은 시간: $lockedDuration")
        }
    }

    private fun checkFailLoginCount(identity: Identity) {
        identity.incrementFailedLoginCount()

        if (identity.failedLoginCount >= MAX_ATTEMPT) {
            val lockedUntil = Instant.now().plusSeconds(LOCK_TIME_IN_SECONDS)
            identity.lock(lockedUntil)
            repo.save(identity)

            val lockedDuration = Duration.between(identity.lockedUntil, Instant.now()).toSeconds()
            throw ErrorCode.UNAUTHORIZED.exception("비밀번호가 유효하지 않습니다. 반복된 로그인 실패로 계정이 잠겼습니다. 남은 시간: $lockedDuration")
        }
    }
}

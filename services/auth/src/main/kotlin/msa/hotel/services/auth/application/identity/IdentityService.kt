package msa.hotel.services.auth.application.identity

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.mail.internet.MimeMessage
import msa.hotel.modules.idgenerator.IdGenerator
import msa.hotel.modules.web.exception.ErrorCode
import msa.hotel.services.auth.application.identity.command.EmailConfirmCommand
import msa.hotel.services.auth.application.identity.command.PasswordChangeCommand
import msa.hotel.services.auth.application.identity.command.RegisterIdentityCommand
import msa.hotel.services.auth.application.identity.command.SendVerifyEmailCommand
import msa.hotel.services.auth.application.identity.dto.IdentityDto
import msa.hotel.services.auth.domain.identity.model.Identity
import msa.hotel.services.auth.domain.identity.model.IdentityId
import msa.hotel.services.auth.domain.identity.model.Status
import msa.hotel.services.auth.domain.identity.policy.EmailVerifyPolicy
import msa.hotel.services.auth.domain.identity.policy.PasswordPolicy
import msa.hotel.services.auth.domain.identity.port.EmailVerifyCodeRepository
import msa.hotel.services.auth.domain.identity.port.IdentityRepository
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class IdentityService(
    private val ids: IdGenerator = IdGenerator(),
    private val repo: IdentityRepository,
    private val emailVerifyTokenRepo: EmailVerifyCodeRepository,
    private val pwEncoder: PasswordEncoder,
    private val mailSender: JavaMailSender,
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

    fun sendVerifyEmail(command: SendVerifyEmailCommand): IdentityDto {
        val identity = checkPendingStatus(command.email.trim().lowercase())

        if (emailVerifyTokenRepo.findCodeByUserId(identity.id.value) != null) {
            throw ErrorCode.CONFLICT.exception("검증 확인 요청 메일 발송 내역이 존재합니다. 메일함을 확인해주세요.")
        }

        val code = UUID.randomUUID().toString()
        emailVerifyTokenRepo.saveCode(userId = identity.id.value, code = code)

        val mimeMessage: MimeMessage = mailSender.createMimeMessage()
        try {
            val mimeMessageHelper = MimeMessageHelper(mimeMessage, false, "UTF-8")
            mimeMessageHelper.setTo(identity.email)
            mimeMessageHelper.setSubject("가입 인증 메일")
            val content =
                EmailVerifyPolicy
                    .mailContent(
                        serverUrl = "http://localhost:8081", // 별도 도메인 구축 전까지 local url 설정
                        email = identity.email,
                        code = code,
                    ).trimIndent()

            mimeMessageHelper.setText(content, true)

            mailSender.send(mimeMessage)
        } catch (e: Exception) {
            logger.error { "Send Verify Email Exception: $e" }
            emailVerifyTokenRepo.deleteCodeByUserId(identity.id.value)
            throw ErrorCode.INTERNAL_SERVER_ERROR.exception("메일 서버 문제로 메일 발송에 실패했습니다.")
        }

        return IdentityDto.from(identity)
    }

    fun checkPendingStatus(email: String): Identity {
        val identity = repo.findActiveByEmail(email) ?: throw ErrorCode.NOT_FOUND.exception("해당 이메일로 등록된 내역이 존재하지 않습니다")

        if (identity.status != Status.PENDING) {
            throw ErrorCode.BAD_REQUEST.exception("해당 계정은 이메일 인증 대기 상태가 아닙니다.")
        }

        return identity
    }

    fun confirmEmail(command: EmailConfirmCommand): IdentityDto {
        val identity = checkPendingStatus(command.email.trim().lowercase())

        val code =
            emailVerifyTokenRepo.findCodeByUserId(identity.id.value)
                ?: throw ErrorCode.CONFLICT.exception("인증 코드가 만료됐습니다. 검증 메일 재발송을 요청하세요.")

        if (code != command.code) {
            throw ErrorCode.VALIDATION_ERROR.exception("요청된 인증 코드가 올바르지 않습니다.")
        }

        identity.verifyMail()
        repo.save(identity)
        emailVerifyTokenRepo.deleteCodeByUserId(identity.id.value)

        return IdentityDto.from(identity)
    }

    fun changePassword(command: PasswordChangeCommand): IdentityDto {
        val userInfo = command.authInfo.tokenUserInfo
        val identity =
            repo.findById(IdentityId(userInfo.userId))
                ?: throw ErrorCode.CONFLICT.exception("인증 정보와 일치하는 등록 정보가 존재하지 않습니다")

        if (!pwEncoder.matches(command.currentPassword, identity.passwordHash)) {
            throw ErrorCode.VALIDATION_ERROR.exception("현재 비밀번호가 올바르지 않습니다")
        }

        val pwValidResult = PasswordPolicy.validate(command.changePassword)
        if (!pwValidResult.isValid) {
            throw ErrorCode.VALIDATION_ERROR.exception("변경하려는 비밀번호가 비밀번호 정책에 위반됩니다 : ${pwValidResult.errorCause}")
        }

        identity.updatePassword(pwEncoder.encode(command.changePassword))
        repo.save(identity)

        return IdentityDto.from(identity)
    }
}

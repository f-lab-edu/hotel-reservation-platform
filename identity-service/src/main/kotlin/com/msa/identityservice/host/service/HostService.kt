package com.msa.identityservice.host.service

import com.msa.identityservice.domain.host.Host
import com.msa.identityservice.domain.host.HostStatus
import com.msa.identityservice.host.repository.HostRepository
import com.msa.identityservice.host.service.dto.RegisterHostDto
import com.msa.supportmodule.exception.BusinessErrorCode
import com.msa.supportmodule.infrastructure.IdGenerator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class HostService(
    private val hostRepository: HostRepository,
    private val idGenerator: IdGenerator,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(registerHostDto: RegisterHostDto): Host {
        checkEmailDuplicateThrow(registerHostDto.email)

        val newHost = registerHostDto.toNewHost(
            newId = idGenerator.generate(),
            encoderPassword = passwordEncoder.encode(registerHostDto.password)
        )

        hostRepository.insert(newHost)

        return newHost
    }

    fun checkEmailDuplicateThrow(email: String) {
        val emailLowercase = email.trim().lowercase()
        val findHost: Host? = hostRepository.findByStatusAndEmail(
            status = HostStatus.ACTIVE,
            email = emailLowercase
        )

        if (findHost != null) {
            throw BusinessErrorCode.CONFLICT.exception("이미 사용 중인 이메일입니다.")
        }
    }

}

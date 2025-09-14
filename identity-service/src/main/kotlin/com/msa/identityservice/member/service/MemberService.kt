package com.msa.identityservice.member.service

import com.msa.identityservice.domain.member.Member
import com.msa.identityservice.domain.member.MemberStatus
import com.msa.identityservice.member.repository.MemberRepository
import com.msa.identityservice.member.service.dto.RegisterMemberDto
import com.msa.supportmodule.exception.BusinessErrorCode
import com.msa.supportmodule.infrastructure.IdGenerator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val idGenerator: IdGenerator,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(registerMemberDto: RegisterMemberDto): Member {
        checkEmailDuplicateThrow(registerMemberDto.email)

        val newMember = registerMemberDto.toNewMember(
            newId = idGenerator.generate(),
            encoderPassword = passwordEncoder.encode(registerMemberDto.password)
        )

        memberRepository.insert(newMember)

        return newMember
    }

    fun checkEmailDuplicateThrow(email: String) {
        val emailLowercase = email.trim().lowercase()
        val findMember: Member? = memberRepository.findByStatusAndEmail(
            status = MemberStatus.ACTIVE,
            email = emailLowercase
        )

        if (findMember != null) {
            throw BusinessErrorCode.CONFLICT.exception("이미 사용 중인 이메일입니다.")
        }
    }

}

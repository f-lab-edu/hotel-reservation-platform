package com.msa.identityservice.member.service

import com.msa.identityservice.infrastructure.IdGenerator
import com.msa.identityservice.jooq.tables.pojos.Member
import com.msa.identityservice.member.repository.MemberRepository
import com.msa.identityservice.member.service.dto.RegisterMemberDto
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

        memberRepository.save(newMember)

        return newMember
    }

    fun checkEmailDuplicateThrow(email: String) {
        memberRepository.findActiveMemberByEmail(email)?.let {
            throw IllegalStateException("이미 사용 중인 이메일입니다.")
        }
    }
}

package com.msa.identityservice.member.repository

import com.msa.identityservice.jooq.enums.MemberStatus
import com.msa.identityservice.jooq.tables.pojos.Member
import com.msa.identityservice.jooq.tables.references.MEMBER
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class MemberRepository(
    private val dsl: DSLContext
) {
    fun save(member: Member) {
        dsl.insertInto(MEMBER)
            .set(MEMBER.ID, member.id)
            .set(MEMBER.EMAIL, member.email)
            .set(MEMBER.PASSWORD, member.password)
            .set(MEMBER.PHONE_NUMBER, member.phoneNumber)
            .set(MEMBER.STATUS, member.status)
            .execute()
    }

    fun findById(id: Long): Member? {
        return dsl.selectFrom(MEMBER)
            .where(MEMBER.ID.eq(id))
            .fetchOneInto(Member::class.java)
    }

    fun findActiveMemberByEmail(email: String): Member? {
        return dsl.selectFrom(MEMBER)
            .where(MEMBER.EMAIL.eq(email).and(MEMBER.STATUS.eq(MemberStatus.ACTIVE)))
            .fetchOneInto(Member::class.java)
    }
}

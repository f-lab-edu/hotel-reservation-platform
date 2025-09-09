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

    fun insert(member: Member) {
        dsl.insertInto(MEMBER)
            .set(MEMBER.ID, member.id)
            .set(MEMBER.EMAIL, member.email)
            .set(MEMBER.PASSWORD, member.password)
            .set(MEMBER.PHONE_NUMBER, member.phoneNumber)
            .execute()
    }

    fun findById(id: Long): Member? {
        return dsl.selectFrom(MEMBER)
            .where(MEMBER.ID.eq(id))
            .fetchOneInto(Member::class.java)
    }

    fun findByStatusAndEmail(status: MemberStatus, email: String): Member? {
        return dsl.selectFrom(MEMBER)
            .where(
                MEMBER.STATUS.eq(status).and(MEMBER.EMAIL.eq(email))
            )
            .fetchOneInto(Member::class.java)
    }

}

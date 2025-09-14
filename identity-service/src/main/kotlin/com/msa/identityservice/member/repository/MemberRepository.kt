package com.msa.identityservice.member.repository

import com.msa.identityservice.domain.member.Member
import com.msa.identityservice.domain.member.MemberStatus
import com.msa.identityservice.jooq.tables.references.MEMBER
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import com.msa.identityservice.jooq.enums.MemberStatus as JooqMemberStatus
import com.msa.identityservice.jooq.tables.pojos.Member as JooqMember

@Repository
class MemberRepository(
    private val dsl: DSLContext
) {

    fun insert(member: Member): Int {
        val jooqMember = toPojo(member)

        return dsl.insertInto(MEMBER)
            .set(MEMBER.ID, jooqMember.id)
            .set(MEMBER.EMAIL, jooqMember.email)
            .set(MEMBER.PASSWORD, jooqMember.password)
            .set(MEMBER.PHONE_NUMBER, jooqMember.phoneNumber)
            .set(MEMBER.STATUS, jooqMember.status)
            .execute()
    }

    fun findById(id: Long): Member? {
        val jooqMember = dsl.selectFrom(MEMBER)
            .where(MEMBER.ID.eq(id))
            .fetchOneInto(JooqMember::class.java)

        if (jooqMember == null) {
            return null
        }

        return toDomain(jooqMember)
    }

    fun findByStatusAndEmail(status: MemberStatus, email: String): Member? {
        val joopStatus = JooqMemberStatus.valueOf(status.name)

        val pojo = dsl.selectFrom(MEMBER)
            .where(MEMBER.STATUS.eq(joopStatus).and(MEMBER.EMAIL.eq(email)))
            .fetchOneInto(JooqMember::class.java)

        if (pojo == null) {
            return null
        }

        return toDomain(pojo)
    }

    private fun toPojo(domain: Member): JooqMember {
        
        return JooqMember(
            id = domain.id,
            email = domain.email,
            password = domain.password,
            phoneNumber = domain.phoneNumber,
            status = JooqMemberStatus.valueOf(domain.status.name),
        )
    }

    private fun toDomain(pojo: JooqMember): Member {

        return Member(
            id = pojo.id,
            email = pojo.email,
            password = pojo.password,
            phoneNumber = pojo.phoneNumber,
            status = MemberStatus.valueOf(pojo.status!!.name)
        )
    }

}

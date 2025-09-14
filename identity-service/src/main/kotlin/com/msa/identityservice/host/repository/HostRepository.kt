package com.msa.identityservice.host.repository

import com.msa.identityservice.domain.host.Host
import com.msa.identityservice.domain.host.HostStatus
import com.msa.identityservice.jooq.tables.references.HOST
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import com.msa.identityservice.jooq.enums.HostStatus as JooqHostStatus
import com.msa.identityservice.jooq.tables.pojos.Host as JooqHost


@Repository
class HostRepository(
    private val dsl: DSLContext
) {

    fun insert(host: Host): Int {
        val jooqHost = toPojo(host)

        return dsl.insertInto(HOST)
            .set(HOST.ID, jooqHost.id)
            .set(HOST.EMAIL, jooqHost.email)
            .set(HOST.PASSWORD, jooqHost.password)
            .set(HOST.PHONE_NUMBER, jooqHost.phoneNumber)
            .set(HOST.STATUS, jooqHost.status)
            .execute()
    }

    fun findById(id: Long): Host? {
        val jooqHost = dsl.selectFrom(HOST)
            .where(HOST.ID.eq(id))
            .fetchOneInto(JooqHost::class.java)

        if (jooqHost == null) {
            return null
        }

        return toDomain(jooqHost)
    }

    fun findByStatusAndEmail(status: HostStatus, email: String): Host? {
        val joopStatus = JooqHostStatus.valueOf(status.name)

        val pojo = dsl.selectFrom(HOST)
            .where(HOST.STATUS.eq(joopStatus).and(HOST.EMAIL.eq(email)))
            .fetchOneInto(JooqHost::class.java)

        if (pojo == null) {
            return null
        }

        return toDomain(pojo)
    }

    private fun toPojo(domain: Host): JooqHost {
        
        return JooqHost(
            id = domain.id,
            email = domain.email,
            password = domain.password,
            phoneNumber = domain.phoneNumber,
            status = JooqHostStatus.valueOf(domain.status.name),
        )
    }

    private fun toDomain(pojo: JooqHost): Host {

        return Host(
            id = pojo.id,
            email = pojo.email,
            password = pojo.password,
            phoneNumber = pojo.phoneNumber,
            status = HostStatus.valueOf(pojo.status!!.name)
        )
    }

}

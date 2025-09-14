package com.msa.accommodationservice.accommodation.repository

import com.msa.accommodationservice.domain.accommodation.Accommodation
import com.msa.identityservice.jooq.tables.references.ACCOMMODATION
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import com.msa.identityservice.jooq.tables.pojos.Accommodation as JooqAccommodation


@Repository
class AccommodationRepository(
    private val dsl: DSLContext
) {

    private fun toJooqPojo(accommodation: Accommodation): JooqAccommodation {
        return JooqAccommodation(
            id = accommodation.id,
            hostid = accommodation.hostId,
            name = accommodation.name,
            description = accommodation.description,
            contactnumber = accommodation.contactNumber,
            city = accommodation.city,
            address = accommodation.address
        )
    }

    private fun toDomain(jooqAccommodation: JooqAccommodation): Accommodation {
        return Accommodation(
            id = jooqAccommodation.id,
            hostId = jooqAccommodation.hostid,
            name = jooqAccommodation.name,
            description = jooqAccommodation.description,
            contactNumber = jooqAccommodation.contactnumber,
            city = jooqAccommodation.city,
            address = jooqAccommodation.address
        )
    }

    fun insert(accommodation: Accommodation): Int {
        val jooqAccommodation = toJooqPojo(accommodation)

        return dsl.insertInto(ACCOMMODATION)
            .set(ACCOMMODATION.ID, jooqAccommodation.id)
            .set(ACCOMMODATION.HOSTID, jooqAccommodation.hostid)
            .set(ACCOMMODATION.NAME, jooqAccommodation.name)
            .set(ACCOMMODATION.DESCRIPTION, jooqAccommodation.description)
            .set(ACCOMMODATION.CONTACTNUMBER, jooqAccommodation.contactnumber)
            .set(ACCOMMODATION.CITY, jooqAccommodation.city)
            .set(ACCOMMODATION.ADDRESS, jooqAccommodation.address)
            .execute()
    }

    fun findAll(): List<Accommodation> {
        val jooqAccommodations = dsl.selectFrom(ACCOMMODATION)
            .fetchInto(JooqAccommodation::class.java)
        
        return jooqAccommodations.map { toDomain(it) }
    }


    fun findById(accommodationId: Long): Accommodation? {
        val jooqAccommodation = dsl.selectFrom(ACCOMMODATION)
            .where(ACCOMMODATION.ID.eq(accommodationId))
            .fetchOneInto(JooqAccommodation::class.java)

        if (jooqAccommodation == null) {
            return null
        }

        return toDomain(jooqAccommodation)
    }

    fun findByNameAndAddress(name: String, address: String): Accommodation? {
        val jooqAccommodation = dsl.selectFrom(ACCOMMODATION)
            .where(ACCOMMODATION.NAME.eq(name).and(ACCOMMODATION.ADDRESS.eq(address)))
            .fetchOneInto(JooqAccommodation::class.java)

        if (jooqAccommodation == null) {
            return null
        }

        return toDomain(jooqAccommodation)
    }


    fun findByHostId(hostId: Long): Accommodation? {
        val jooqAccommodation = dsl.selectFrom(ACCOMMODATION)
            .where(ACCOMMODATION.HOSTID.eq(hostId))
            .fetchOneInto(JooqAccommodation::class.java)

        if (jooqAccommodation == null) {
            return null
        }

        return toDomain(jooqAccommodation)
    }

    fun update(updateAccommodation: Accommodation): Int {
        val jooqAccommodation = toJooqPojo(updateAccommodation)

        return dsl.update(ACCOMMODATION)
            .set(ACCOMMODATION.NAME, jooqAccommodation.name)
            .set(ACCOMMODATION.DESCRIPTION, jooqAccommodation.description)
            .set(ACCOMMODATION.CONTACTNUMBER, jooqAccommodation.contactnumber)
            .set(ACCOMMODATION.CITY, jooqAccommodation.city)
            .set(ACCOMMODATION.ADDRESS, jooqAccommodation.address)
            .execute()
    }


}

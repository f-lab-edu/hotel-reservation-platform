package com.msa.accommodationservice.accommodation.service.dto

import com.msa.accommodationservice.domain.accommodation.Accommodation


data class AccommodationDto(
    val hostId: Long,
    val name: String,
    val description: String,
    val contactNumber: String,
    val city: String,
    val address: String,
) {

    fun toAccommodation(newId: Long): Accommodation {
        return Accommodation(
            id = newId,
            hostId = hostId,
            name = name,
            description = description,
            contactNumber = contactNumber,
            city = city,
            address = address
        )
    }

}

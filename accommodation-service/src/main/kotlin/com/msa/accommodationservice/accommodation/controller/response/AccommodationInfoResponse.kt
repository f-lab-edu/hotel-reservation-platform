package com.msa.accommodationservice.accommodation.controller.response

data class AccommodationInfoResponse(
    val accommodationId: Long,
    val hostId: Long,
    val name: String,
    val description: String,
    val contactNumber: String,
    val city: String,
    val address: String,
)

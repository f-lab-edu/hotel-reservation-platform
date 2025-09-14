package com.msa.accommodationservice.accommodation.controller.request

import com.msa.accommodationservice.accommodation.service.dto.AccommodationDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class AccommodationUpdateRequest(
    @field:NotBlank(message = "숙소 이름은 필수입니다.")
    @field:Size(max = 255, message = "숙소 이름은 255자를 넘길 수 없습니다")
    val name: String?,

    @field:NotBlank(message = "숙소 설명은 필수입니다.")
    @field:Size(max = 10000, message = "숙소 설명은 10,000자를 넘길 수 없습니다")
    val description: String?,

    @field:NotBlank(message = "숙소 연락처는 필수입니다.")
    @field:Size(max = 20, message = "숙소 연락처은 20자를 넘길 수 없습니다")
    val contactNumber: String?,

    @field:NotBlank(message = "숙소 도시 정보는 필수입니다.")
    @field:Size(max = 20, message = "숙소 도시 정보는 20자를 넘길 수 없습니다")
    val city: String?,

    @field:NotBlank(message = "숙소 주소 정보는 필수입니다.")
    @field:Size(max = 20, message = "숙소 주소 정보는 255자를 넘길 수 없습니다")
    val address: String?,
) {

    fun toAccommodationDto(hostId: Long) = AccommodationDto(
        hostId = hostId,
        name = name!!,
        description = description!!,
        contactNumber = contactNumber!!,
        city = city!!,
        address = address!!
    )

}

package com.msa.accommodationservice.accommodation.controller

import com.msa.accommodationservice.accommodation.controller.request.AccommodationRegistrationRequest
import com.msa.accommodationservice.accommodation.controller.request.AccommodationUpdateRequest
import com.msa.accommodationservice.accommodation.controller.response.AccommodationInfoResponse
import com.msa.accommodationservice.accommodation.controller.response.AccommodationInfosResponse
import com.msa.accommodationservice.accommodation.service.AccommodationService
import com.msa.supportmodule.annotation.HostId
import com.msa.supportmodule.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/accommodations")
class AccommodationController(
    private val service: AccommodationService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @HostId hostId: Long,
        @Valid @RequestBody request: AccommodationRegistrationRequest,
    ): ApiResponse<AccommodationInfoResponse> {
        val dto = request.toAccommodationDto(hostId)

        val accommodation = service.register(dto)

        val response = AccommodationInfoResponse(
            accommodationId = accommodation.id,
            name = accommodation.name,
            description = accommodation.description,
            contactNumber = accommodation.contactNumber,
            city = accommodation.city,
            address = accommodation.address,
            hostId = accommodation.hostId,
        )

        return ApiResponse.create(
            message = "숙소 정보를 등록 했습니다.",
            data = response
        )
    }

    @PatchMapping()
    fun update(
        @HostId hostId: Long,
        @Valid @RequestBody request: AccommodationUpdateRequest,
    ): ApiResponse<AccommodationInfoResponse> {
        val dto = request.toAccommodationDto(hostId)

        val accommodation = service.update(dto)
        val response = AccommodationInfoResponse(
            accommodationId = accommodation.id,
            name = accommodation.name,
            description = accommodation.description,
            contactNumber = accommodation.contactNumber,
            city = accommodation.city,
            address = accommodation.address,
            hostId = accommodation.hostId,
        )

        return ApiResponse.update(
            message = "숙소 정보를 수정 했습니다.",
            data = response
        )
    }

    // TODO: 서치 및 페이징 처리
    @GetMapping()
    fun getAccommodations(): ApiResponse<AccommodationInfosResponse> {
        val accommodations = service.getAccommodations()

        val responseInfos = accommodations.map { accommodation ->
            AccommodationInfoResponse(
                accommodationId = accommodation.id,
                name = accommodation.name,
                description = accommodation.description,
                contactNumber = accommodation.contactNumber,
                city = accommodation.city,
                address = accommodation.address,
                hostId = accommodation.hostId,
            )
        }
        val response = AccommodationInfosResponse(
            accommodations = responseInfos
        )

        return ApiResponse.read(
            message = "숙소 리스트 조회에 성공 했습니다.",
            data = response
        )
    }

    @GetMapping("{accommodationId}")
    fun getAccommodation(
        @PathVariable accommodationId: Long
    ): ApiResponse<AccommodationInfoResponse> {
        val accommodation = service.getAccommodation(accommodationId)
        val response = AccommodationInfoResponse(
            accommodationId = accommodation.id,
            name = accommodation.name,
            description = accommodation.description,
            contactNumber = accommodation.contactNumber,
            city = accommodation.city,
            address = accommodation.address,
            hostId = accommodation.hostId,
        )

        return ApiResponse.read(
            message = "숙소 정보를 조회에 성공했습니다.",
            data = response
        )
    }

    @GetMapping("me")
    fun getMyAccommodation(
        @HostId hostId: Long
    ): ApiResponse<AccommodationInfoResponse> {
        val accommodation = service.getAccommodationByHostId(hostId)
        val response = AccommodationInfoResponse(
            accommodationId = accommodation.id,
            name = accommodation.name,
            description = accommodation.description,
            contactNumber = accommodation.contactNumber,
            city = accommodation.city,
            address = accommodation.address,
            hostId = accommodation.hostId,
        )

        return ApiResponse.read(
            message = "숙소 정보를 조회에 성공했습니다.",
            data = response
        )
    }

}

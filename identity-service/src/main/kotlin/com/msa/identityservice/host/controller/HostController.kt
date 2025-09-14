package com.msa.identityservice.host.controller

import com.msa.identityservice.host.controller.request.HostRegistrationRequest
import com.msa.identityservice.host.controller.response.HostRegistrationResponse
import com.msa.identityservice.host.service.HostService
import com.msa.supportmodule.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/hosts")
class HostController(
    private val hostService: HostService
) {

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody
        request: HostRegistrationRequest
    ): ApiResponse<HostRegistrationResponse> {
        val dto = request.toRegisterHostDto()
        val newHost = hostService.register(dto)

        val response = HostRegistrationResponse(
            id = newHost.id,
            email = newHost.email
        )

        return ApiResponse.create(
            message = "회원 가입에 성공했습니다.",
            data = response
        )
    }

}

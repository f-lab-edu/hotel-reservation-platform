package msa.hotel.services.auth.infrastructure.web.identity

import jakarta.validation.Valid
import msa.hotel.modules.web.response.Response
import msa.hotel.services.auth.application.identity.IdentityService
import msa.hotel.services.auth.infrastructure.web.identity.request.RegisterIdentityRequest
import msa.hotel.services.auth.infrastructure.web.identity.response.RegisterIdentityResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/identity")
class IdentityController(
    private val identityService: IdentityService,
) {
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody
        request: RegisterIdentityRequest,
    ): Response<RegisterIdentityResponse> {
        val command = request.toRegisterCommand()
        val identityDto = identityService.register(command)
        val data = RegisterIdentityResponse.from(identityDto)

        return Response.create(
            message = "Identity 정보를 등록 했습니다.",
            data = data,
        )
    }
}

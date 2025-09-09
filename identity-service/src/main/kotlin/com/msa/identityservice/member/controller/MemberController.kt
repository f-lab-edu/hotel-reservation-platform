package com.msa.identityservice.member.controller

import com.msa.identityservice.member.controller.request.MemberRegistrationRequest
import com.msa.identityservice.member.controller.response.MemberRegistrationResponse
import com.msa.identityservice.member.service.MemberService
import com.msa.identityservice.support.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/members")
class MemberController(
    private val memberService: MemberService
) {

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody
        request: MemberRegistrationRequest
    ): ApiResponse<MemberRegistrationResponse> {
        val registerMemberDto = request.toRegisterMemberDto()
        val newMember = memberService.register(registerMemberDto)

        val response = MemberRegistrationResponse(
            id = newMember.id,
            email = newMember.email
        )

        return ApiResponse.create(
            message = "회원 가입에 성공했습니다.",
            data = response
        )
    }

}

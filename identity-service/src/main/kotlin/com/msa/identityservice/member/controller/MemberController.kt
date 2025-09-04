package com.msa.identityservice.member.controller

import com.msa.identityservice.member.controller.request.MemberRegistrationRequest
import com.msa.identityservice.member.controller.response.MemberRegistrationResponse
import com.msa.identityservice.member.service.MemberService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/members")
class MemberController(
    private val memberService: MemberService
) {
    @PostMapping()
    fun register(@RequestBody request: MemberRegistrationRequest): ResponseEntity<MemberRegistrationResponse> {
        val registerMemberDto = request.validateToRegisterMemberDto()
        val newMember = memberService.register(registerMemberDto)
        val response = MemberRegistrationResponse(
            id = newMember.id,
            email = newMember.email
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}

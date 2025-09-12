package com.msa.identityservice.member

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.identityservice.exception.BusinessErrorCode
import com.msa.identityservice.jooq.enums.MemberStatus
import com.msa.identityservice.member.controller.request.MemberRegistrationRequest
import com.msa.identityservice.member.repository.MemberRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc // MockMvc를 실제 서버처럼 사용하기 위한 설정
@ActiveProfiles("test")
@Transactional
class MemberControllerIntegrationTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val memberRepository: MemberRepository,
    val passwordEncoder: PasswordEncoder,
) : BehaviorSpec({

    Given("신규 멤버 정보") {
        val request = MemberRegistrationRequest(
            email = "${UUID.randomUUID()}@example.com",
            password = "Password123!",
            phoneNumber = "010-1234-5678"
        )

        When("신규 멤버 가입 API 요청") {
            val result = mockMvc.post("/members") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isCreated() } // 201 Created 상태인지 확인
                jsonPath("$.success") { value(true) }
                jsonPath("$.code") { value("create") }
                jsonPath("$.data.email") { value(request.email) }
            }.andReturn()

            Then("신규 멤버 정보 DB에 저장") {
                val responseBody = objectMapper.readTree(result.response.contentAsString)
                val memberId = responseBody.get("data").get("id").asLong()
                val savedMember = memberRepository.findById(memberId)
                savedMember shouldNotBe null
                savedMember?.email shouldBe request.email
                savedMember?.phoneNumber shouldBe request.phoneNumber
                savedMember?.status shouldBe MemberStatus.ACTIVE
                passwordEncoder.matches(request.password, savedMember?.password) shouldBe true // 비밀번호가 암호화되었는지 확인
            }
        }

        When("이미 가입된 이후 재가입 시도") {
            val result = mockMvc.post("/members") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isConflict() } // 409 Conflict 상태인지 확인
                jsonPath("$.success") { value(false) }
                jsonPath("$.code") { value(BusinessErrorCode.CONFLICT.name) }
            }.andReturn()

            Then("이메일 중복으로 가입 실패 확인") {
                val responseBody = objectMapper.readTree(result.response.contentAsString)
                val message = responseBody.get("message").asText()
                message shouldBe "이미 사용 중인 이메일입니다."
            }
        }
    }

})


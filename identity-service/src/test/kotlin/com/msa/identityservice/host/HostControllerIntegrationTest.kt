package com.msa.identityservice.host

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.identityservice.domain.host.HostStatus
import com.msa.identityservice.host.repository.HostRepository
import com.msa.identityservice.member.controller.request.MemberRegistrationRequest
import com.msa.supportmodule.exception.BusinessErrorCode
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
class HostControllerIntegrationTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val hostRepository: HostRepository,
    val passwordEncoder: PasswordEncoder,
) : BehaviorSpec({

    Given("신규 호스트 정보 Registration") {
        val request = MemberRegistrationRequest(
            email = "${UUID.randomUUID()}@example.com",
            password = "Password123!",
            phoneNumber = "010-1234-5678"
        )

        When("신규 호스트 가입 API 요청") {
            val result = mockMvc.post("/hosts") {
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
                val hostId = responseBody.get("data").get("id").asLong()
                val savedHost = hostRepository.findById(hostId)
                savedHost shouldNotBe null
                savedHost?.email shouldBe request.email
                savedHost?.phoneNumber shouldBe request.phoneNumber
                savedHost?.status shouldBe HostStatus.ACTIVE
                passwordEncoder.matches(request.password, savedHost?.password) shouldBe true // 비밀번호가 암호화되었는지 확인
            }
        }

        When("이미 가입된 이후 재가입 시도") {
            val result = mockMvc.post("/hosts") {
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

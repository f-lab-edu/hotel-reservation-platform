package com.msa.identityservice.member

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.identityservice.exception.BusinessErrorCode
import com.msa.identityservice.jooq.enums.MemberStatus
import com.msa.identityservice.jooq.tables.pojos.Member
import com.msa.identityservice.member.controller.request.MemberRegistrationRequest
import com.msa.identityservice.member.repository.MemberRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc // MockMvc를 실제 서버처럼 사용하기 위한 설정
@Transactional
class MemberControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val memberRepository: MemberRepository, // DB 상태 검증을 위해 실제 Repository 주입
    private val passwordEncoder: PasswordEncoder
) {

    @Test
    fun `회원 가입에 성공하고, 데이터베이스에 암호화된 비밀번호로 저장된다`() {
        // Given
        val request = MemberRegistrationRequest(
            email = "test@example.com",
            password = "Password123!",
            phoneNumber = "010-1234-5678"
        )

        // When
        val result = mockMvc.post("/members") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() } // 201 Created 상태인지 확인
            jsonPath("$.success") { value(true) }
            jsonPath("$.code") { value("create") }
            jsonPath("$.data.email") { value(request.email) }

        }.andReturn()

        // Then: DB에서 직접 데이터를 조회하여 검증
        val responseBody = objectMapper.readTree(result.response.contentAsString)
        val memberId = responseBody.get("data").get("id").asLong()
        val savedMember = memberRepository.findById(memberId)

        savedMember shouldNotBe null
        savedMember?.email shouldBe request.email
        savedMember?.phoneNumber shouldBe request.phoneNumber
        savedMember?.status shouldBe MemberStatus.ACTIVE
        passwordEncoder.matches(request.password, savedMember?.password) shouldBe true // 비밀번호가 암호화되었는지 확인
    }

    @Test
    fun `이미 존재하는 이메일로 가입을 시도하면 409 Conflict와 에러 응답을 반환한다`() {
        // Given: 먼저 사용자를 하나 저장해 둠
        val existingEmail = "test@example.com"
        val existingMember = Member(
            id = 1L,
            email = existingEmail,
            password = "password",
            phoneNumber = "010-1234-5678",
            status = MemberStatus.ACTIVE,
        )
        memberRepository.save(existingMember)

        val request = MemberRegistrationRequest(existingEmail, "Password123!", "010-1111-2222")

        // When & Then
        mockMvc.post("/members") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isConflict() } // 409 Conflict 상태인지 확인
            // ApiResponse 에러 형식 검증
            jsonPath("$.success") { value(false) }
            jsonPath("$.code") { value(BusinessErrorCode.CONFLICT.name) }
            jsonPath("$.message") { value("이미 사용 중인 이메일입니다.") }
        }
    }
}

package com.msa.identityservice.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.identityservice.auth.controller.request.LoginRequest
import com.msa.identityservice.auth.service.AuthService.Companion.ACTIVE_KEY_PREFIX
import com.msa.identityservice.auth.service.AuthService.Companion.REFRESH_TOKEN_PREFIX
import com.msa.identityservice.auth.token.JwtTokenProvider
import com.msa.identityservice.auth.token.enums.Role
import com.msa.identityservice.jooq.enums.MemberStatus
import com.msa.identityservice.jooq.tables.pojos.Member
import com.msa.identityservice.member.repository.MemberRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>
) {
    lateinit var testMember: Member

    @BeforeEach
    fun setup() {
        testMember = Member(
            id = 1L,
            email = "test@example.com",
            password = passwordEncoder.encode("password1!"),
            status = MemberStatus.ACTIVE,
        )
        memberRepository.insert(testMember)
    }

    @Test
    fun `정상적인 정보로 로그인 요청 시, 토큰이 발급되고 세션 정보가 Redis에 저장된다`() {
        // Given
        val deviceId = "test-device-1"
        val request = LoginRequest(
            email = "test@example.com",
            password = "password1!",
            role = Role.MEMBER,
            deviceId = deviceId
        )

        // When
        val result = mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNoContent() } // 204 No Content
            header { exists("Authorization") }
            cookie { exists("refreshToken") }
        }.andReturn()

        // Then: Token 검증 및 Redis에 데이터가 올바르게 저장되었는지 직접 검증
        val accessToken = result.response.getHeader("Authorization")!!.substring(7)
        val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(accessToken)
        tokenAuthInfo.userId shouldBe testMember.id
        tokenAuthInfo.role shouldBe Role.MEMBER
        tokenAuthInfo.deviceId shouldBe deviceId
        tokenAuthInfo.email shouldBe testMember.email

        val userRefreshTokensKey = "$REFRESH_TOKEN_PREFIX:${Role.MEMBER.name.lowercase()}:${testMember.id}"
        val savedRefreshTokenInfo = redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, deviceId)
        savedRefreshTokenInfo shouldNotBe null

        val activeTokenKey = "$ACTIVE_KEY_PREFIX:${Role.MEMBER.name.lowercase()}:${testMember.id}:$deviceId"
        val savedActiveToken = redisTemplate.opsForValue().get(activeTokenKey)
        savedActiveToken shouldBe testMember.id.toString()
    }

    @Test
    fun `유효한 리프레시 토큰으로 토큰 재발급 요청 시, 새로운 토큰들이 발급된다`() {
        // Given: 먼저 로그인을 수행하여 유효한 토큰들을 발급받음
        val deviceId = "test-device-2"
        val (accessToken, refreshTokenCookie) = performLoginAndGetTokens(deviceId)

        // When
        val result = mockMvc.get("/auth/refresh") {
            cookie(refreshTokenCookie)
            header("Authorization", "Bearer $accessToken") // 재발급 시에도 만료된 AccessToken은 보내야 함
        }.andExpect {
            status { isNoContent() }
        }.andReturn()

        // Then
        val newAccessToken = result.response.getHeader("Authorization")
        val newRefreshTokenCookie = result.response.getCookie("refreshToken")

        newAccessToken shouldNotBe "Bearer $accessToken"
        newRefreshTokenCookie?.value shouldNotBe refreshTokenCookie.value
    }

    @Test
    fun `유효한 액세스 토큰으로 내 정보 조회 요청 시, 사용자 정보가 반환된다`() {
        // Given
        val deviceId = "test-device-3"
        val (accessToken, _) = performLoginAndGetTokens(deviceId)

        // When & Then
        mockMvc.get("/auth/me") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.userId") { value(testMember.id) }
            jsonPath("$.data.email") { value(testMember.email) }
            jsonPath("$.data.role") { value(Role.MEMBER.name) }
            jsonPath("$.data.deviceId") { value(deviceId) }
        }
    }

    // 테스트 코드 중복을 줄이기 위한 헬퍼 함수
    private fun performLoginAndGetTokens(deviceId: String): Pair<String, Cookie> {
        val request = LoginRequest(testMember.email, "password1!", Role.MEMBER, deviceId)
        val result = mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andReturn()

        val accessToken = result.response.getHeader("Authorization")!!.substring(7)
        val refreshTokenCookie = result.response.getCookie("refreshToken")!!
        return Pair(accessToken, refreshTokenCookie)
    }
}

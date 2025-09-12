package com.msa.gatewayservice

import com.msa.gatewayservice.auth.consts.AuthConstants.getActiveJtiKey
import com.msa.gatewayservice.auth.token.JwtTokenDecoder
import com.msa.gatewayservice.auth.token.dto.TokenAuthInfo
import com.msa.identityservice.auth.token.enums.Role
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewayIntegrationTest(
    private val webTestClient: WebTestClient,
    private val jwtTokenDecoder: JwtTokenDecoder,
    private val redisTemplate: RedisTemplate<String, String>
) : BehaviorSpec({

    lateinit var mockWebServer: MockWebServer

    beforeSpec {
        mockWebServer = MockWebServer()
        mockWebServer.start(8001)
    }

    afterSpec {
        mockWebServer.shutdown()
    }

    Given("Gateway에 공개 경로와 보안 경로가 설정되어 있을 때") {

        When("클라이언트가 인증이 필요 없는 공개 경로(회원가입)로 요청을 보내면") {
            mockWebServer.enqueue(MockResponse().setResponseCode(201))

            val response = webTestClient.post().uri("/identity-service/members")
                .exchange()

            Then("인증 필터를 거치지 않고, 요청이 올바르게 전달된다") {
                response.expectStatus().isCreated
                mockWebServer.takeRequest().path shouldBe "/members"
            }
        }

        When("클라이언트가 유효한 토큰과 함께 보안 경로로 요청을 보내면") {
            val validToken = "valid-jwt"
            val mockAuthInfo = TokenAuthInfo(
                userId = 1L,
                role = Role.MEMBER,
                email = "test@test.com",
                deviceId = "device-1",
                jti = "jti-1",
                expiresAt = Date()
            )

            // JwtTokenProvider Mock 설정
            every { jwtTokenDecoder.extractAuthInfo(any()) } returns mockAuthInfo

            val activeJtiKey = getActiveJtiKey(mockAuthInfo.jti)
            redisTemplate.opsForValue().set(activeJtiKey, mockAuthInfo.userId.toString())

            mockWebServer.enqueue(MockResponse().setResponseCode(200))

            val response = webTestClient.get().uri("/identity-service/test")
                .header("Authorization", "Bearer $validToken")
                .exchange()

            Then("인증 필터를 통과하고, 사용자 정보 헤더가 추가되어 요청이 전달된다") {
                response.expectStatus().isOk
                val recordedRequest = mockWebServer.takeRequest()
                recordedRequest.path shouldBe "/test"
                recordedRequest.getHeader("X-User-Id") shouldBe "1"
                recordedRequest.getHeader("X-User-Role") shouldBe "MEMBER"
                recordedRequest.getHeader("X-User-Email") shouldBe "test@test.com"
            }

            // 불필요해진 Redis 데이터 삭제
            redisTemplate.delete(activeJtiKey)
        }

        When("클라이언트가 토큰 없이 보안 경로로 요청을 보내면") {
            val response = webTestClient.get().uri("/identity-service/test")
                .exchange()

            Then("Gateway는 401 Unauthorized 에러를 즉시 반환한다") {
                response.expectStatus().isUnauthorized
            }
        }
    }

}) {
    
    // 테스트 환경에서 실제 JwtTokenProvider 대신 Mock Bean을 사용하도록 설정
    @TestConfiguration
    class TestJwtConfig {
        @Bean
        fun jwtTokenDecoder(): JwtTokenDecoder = mockk(relaxed = true)
    }

}

package com.msa.identityservice.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.identityservice.auth.consts.AuthConstants.AUTH_HEADER_NAME
import com.msa.identityservice.auth.consts.AuthConstants.AUTH_HEADER_PREFIX
import com.msa.identityservice.auth.consts.AuthConstants.REFRESH_COOKIE_NAME
import com.msa.identityservice.auth.consts.AuthConstants.getAccessTokenHeaderValue
import com.msa.identityservice.auth.consts.AuthConstants.getActiveJtiKey
import com.msa.identityservice.auth.consts.AuthConstants.getRefreshTokenKey
import com.msa.identityservice.auth.consts.AuthConstants.getSessionKey
import com.msa.identityservice.auth.controller.request.LoginRequest
import com.msa.identityservice.auth.controller.request.LogoutRequest
import com.msa.identityservice.auth.token.JwtTokenProvider
import com.msa.identityservice.auth.token.enums.Role
import com.msa.identityservice.config.properties.JwtProperties
import com.msa.identityservice.member.service.MemberService
import com.msa.identityservice.member.service.dto.RegisterMemberDto
import io.kotest.core.spec.KotestTestScope
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.servlet.http.Cookie
import org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.lang.Thread.sleep

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@KotestTestScope
class AuthControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val memberService: MemberService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtProperties: JwtProperties
) : BehaviorSpec({

    fun deleteLoginHistory(accessToken: String) {
        val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(accessToken)

        // 활성 토큰 내역 KEY 삭제
        val activeJtiKey = getActiveJtiKey(tokenAuthInfo.jti)
        redisTemplate.delete(activeJtiKey)

        // 리프레쉬 토큰 정보 HASH 삭제
        val userRefreshTokensKey = getRefreshTokenKey(tokenAuthInfo.role, tokenAuthInfo.userId)
        redisTemplate.opsForHash<String, String>().delete(userRefreshTokensKey, tokenAuthInfo.deviceId)

        // 리프레쉬 토큰 세션 만료 ZSET 내역 삭제
        val sessionKey = getSessionKey(tokenAuthInfo.role, tokenAuthInfo.userId)
        redisTemplate.opsForZSet().remove(sessionKey, tokenAuthInfo.deviceId)
    }

    fun performLogin(loginRequest: LoginRequest) = mockMvc.post("/auth/login") {
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(loginRequest)
    }

    fun performLoginAndGetTokens(loginRequest: LoginRequest): Pair<String, Cookie> {
        val result = performLogin(loginRequest).andReturn()
        val accessToken = result.response.getHeader("Authorization")!!.substring(7)
        val refreshTokenCookie = result.response.getCookie("refreshToken")!!
        return Pair(accessToken, refreshTokenCookie)
    }

    fun createLoginRequest(registerMemberDto: RegisterMemberDto, deviceId: String) = LoginRequest(
        email = registerMemberDto.email,
        password = registerMemberDto.password,
        role = Role.MEMBER,
        deviceId = deviceId
    )

    Given("가입된 멤버 및 로그인 디바이스 정보") {
        val registerMemberDto = RegisterMemberDto(
            email = "test@email.com",
            password = "test1234!",
            phoneNumber = "010-9999-9999"
        )
        val testMember = memberService.register(registerMemberDto)
        val loginDeviceId = "test-device-1"

        When("동시 접속 기기가 없을 경우의 로그인 요청") {
            val request = createLoginRequest(registerMemberDto, loginDeviceId)

            val result = mockMvc.post("/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isNoContent() } // 204 No Content
                header { exists(AUTH_HEADER_NAME) }
                cookie { exists(REFRESH_COOKIE_NAME) }
            }.andReturn()

            Then("인증 토큰 발급 및 활성 토큰 정보 Redis 저장") {
                val accessToken = result.response.getHeader(AUTH_HEADER_NAME)!!.substring(AUTH_HEADER_PREFIX.length)
                val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(accessToken)
                tokenAuthInfo.userId shouldBe testMember.id
                tokenAuthInfo.role shouldBe Role.MEMBER
                tokenAuthInfo.deviceId shouldBe loginDeviceId
                tokenAuthInfo.email shouldBe testMember.email

                val userRefreshTokensKey = getRefreshTokenKey(Role.MEMBER, testMember.id)
                val savedRefreshTokenInfo =
                    redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, loginDeviceId)
                savedRefreshTokenInfo shouldNotBe null

                val activeJtiKey = getActiveJtiKey(tokenAuthInfo.jti)
                val savedActiveJtiValue = redisTemplate.opsForValue().get(activeJtiKey)
                savedActiveJtiValue shouldBe testMember.id.toString()

                // 불필요한 Redis 데이터 삭제
                deleteLoginHistory(accessToken)
            }
        }

        When("최대 접속 기기까지 로그인 상태일 때 로그인 요청") {
            val maxDeviceCount = jwtProperties.maxDeviceCount
            val accessTokenList = mutableListOf<String>()
            for (i in 1..maxDeviceCount) {
                sleep(100)
                val loginRequest = createLoginRequest(registerMemberDto, "device-$i")
                val (accessToken, _) = performLoginAndGetTokens(loginRequest)
                accessTokenList.add(accessToken)
            }

            val userRefreshTokensKey = getRefreshTokenKey(Role.MEMBER, testMember.id)
            redisTemplate.opsForHash<String, String>().size(userRefreshTokensKey) shouldBe maxDeviceCount

            val newDeviceId = "device-${maxDeviceCount + 1}"
            val request = createLoginRequest(registerMemberDto, newDeviceId)

            val result = mockMvc.post("/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isNoContent() } // 로그인은 성공해야 함
            }.andReturn()

            Then("제일 오래전에 접속한 기기 세션 자동 로그아웃") {
                // 1. 전체 세션 수는 여전히 최대 기기 수와 같아야 함
                redisTemplate.opsForHash<String, String>().size(userRefreshTokensKey) shouldBe maxDeviceCount
                // 2. 가장 오래된 세션(device-1)은 삭제되어야 함
                redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, "device-1") shouldBe null
                // 3. 새로 로그인한 세션(newDeviceId)은 존재해야 함
                redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, newDeviceId) shouldNotBe null

                // 불필요해진 데이터 삭제
                accessTokenList.forEach { accessToken -> deleteLoginHistory(accessToken) }
                val accessToken = result.response.getHeader(AUTH_HEADER_NAME)!!.substring(AUTH_HEADER_PREFIX.length)
                deleteLoginHistory(accessToken)
            }
        }

        When("유효한 리프레시 토큰으로 토큰 재발급 요청") {
            val loginRequest = createLoginRequest(registerMemberDto, loginDeviceId)
            val (accessToken, refreshTokenCookie) = performLoginAndGetTokens(loginRequest)

            val result = mockMvc.get("/auth/refresh") {
                cookie(refreshTokenCookie)
                header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessToken)) // 재발급 시에도 AccessToken 확인
            }.andExpect {
                status { isNoContent() }
            }.andReturn()

            Then("새로운 토큰 발급 확인") {
                val newAccessToken = result.response.getHeader(AUTH_HEADER_NAME)!!.substring(AUTH_HEADER_PREFIX.length)
                val newRefreshTokenCookie = result.response.getCookie(REFRESH_COOKIE_NAME)

                newAccessToken shouldNotBe accessToken
                newRefreshTokenCookie?.value shouldNotBe refreshTokenCookie.value

                // 불필요해진 데이터 삭제
                deleteLoginHistory(newAccessToken)
            }
        }

        When("유효하지 않은(위변조된) 리프레시 토큰으로 재발급 요청") {
            val loginRequest = createLoginRequest(registerMemberDto, loginDeviceId)
            val (accessToken, _) = performLoginAndGetTokens(loginRequest)
            // 위변조된 가짜 리프레시 토큰 생성
            val fakeRefreshTokenCookie = Cookie("refreshToken", "this.is.fake-token")

            val result = mockMvc.get("/auth/refresh") {
                cookie(fakeRefreshTokenCookie)
                header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessToken))
            }.andExpect {
                status { isUnauthorized() } // 401 Unauthorized
            }.andReturn()

            Then("올바르지 않는 인증 정보인 점을 응답") {
                val responseBody = objectMapper.readTree(result.response.contentAsString)
                val message = responseBody.get("message").asText()
                message shouldBe "인증 정보가 올바르지 않습니다."

                // 불필요해진 데이터 삭제
                deleteLoginHistory(accessToken)
            }
        }

        When("유효한 인증 토큰으로 내 정보 조회 요청") {
            val loginRequest = createLoginRequest(registerMemberDto, loginDeviceId)
            val (accessToken, _) = performLoginAndGetTokens(loginRequest)

            val result = mockMvc.get("/auth/me") {
                header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessToken))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.userId") { value(testMember.id) }
                jsonPath("$.data.email") { value(testMember.email) }
                jsonPath("$.data.role") { value(Role.MEMBER.name) }
                jsonPath("$.data.deviceId") { value(loginRequest.deviceId) }
            }.andReturn()

            Then("기본 로그인 사용자 정보가 반환") {
                val responseBody = objectMapper.readTree(result.response.contentAsString)
                val userId = responseBody.get("data").get("userId").asLong()
                userId shouldBe testMember.id
                val email = responseBody.get("data").get("email").asText()
                email shouldBe testMember.email
                val deviceId = responseBody.get("data").get("deviceId").asText()
                deviceId shouldBe loginDeviceId

                // 불필요해진 데이터 삭제
                deleteLoginHistory(accessToken)
            }
        }

        When("로그인된 사용자가 자신의 활성 세션 목록 요청") {
            val deviceIdA = "Device-A"
            val loginRequestFromA = createLoginRequest(registerMemberDto, deviceIdA)
            val (accessTokenFromA, _) = performLoginAndGetTokens(loginRequestFromA)

            val deviceIdB = "Device-B"
            val loginRequestFromB = createLoginRequest(registerMemberDto, deviceIdB)
            val (accessTokenFromB, _) = performLoginAndGetTokens(loginRequestFromB)

            val result = mockMvc.get("/auth/sessions") {
                header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessTokenFromB))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }.andReturn()

            Then("세션 응답 정보에 접속 중인 기기 정보 포함") {
                val responseBody = objectMapper.readTree(result.response.contentAsString)
                val deviceIds = responseBody.get("data").get("sessions").findValuesAsText("deviceId")
                deviceIds.size shouldBe 2
                deviceIds.contains(deviceIdA) shouldBe true
                deviceIds.contains(deviceIdB) shouldBe true

                // 불필요해진 데이터 삭제
                deleteLoginHistory(accessTokenFromA)
                deleteLoginHistory(accessTokenFromB)
            }
        }

        When("로그인된 사용자가 로그아웃 요청") {
            val loginRequest = createLoginRequest(registerMemberDto, loginDeviceId)
            val (accessToken, refreshTokenCookie) = performLoginAndGetTokens(loginRequest)

            val result = mockMvc.delete("/auth/logout") {
                header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessToken))
                cookie(refreshTokenCookie) // 로그아웃 시에도 본인 확인을 위해 쿠키는 필요
            }.andExpect {
                // Then
                status { isOk() }
                // 쿠키가 삭제되었는지 확인 (Max-Age=0)
                cookie {
                    maxAge(REFRESH_COOKIE_NAME, 0)
                }
            }.andReturn()

            Then("기존 인증 토큰이 무효화되기 위해 Redis 로그인 세션 정보가 삭제됨") {
                val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(accessToken)
                val activeJtiKey = getActiveJtiKey(tokenAuthInfo.jti)
                redisTemplate.opsForValue().get(activeJtiKey) shouldBe null

                val userRefreshTokensKey = getRefreshTokenKey(tokenAuthInfo.role, tokenAuthInfo.userId)
                redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, loginDeviceId) shouldBe null

                val userSessionAgesKey = getSessionKey(tokenAuthInfo.role, tokenAuthInfo.userId)
                redisTemplate.opsForZSet().score(userSessionAgesKey, loginDeviceId) shouldBe null
            }
        }

        When("다른 기기 원격 로그아웃 요청 시") {
            val deviceIdA = "Device-A"
            val loginRequestFromA = createLoginRequest(registerMemberDto, deviceIdA)
            val (accessTokenFromA, _) = performLoginAndGetTokens(loginRequestFromA)

            val deviceIdB = "Device-B"
            val loginRequestFromB = createLoginRequest(registerMemberDto, deviceIdB)
            val (accessTokenFromB, _) = performLoginAndGetTokens(loginRequestFromB)

            val request = LogoutRequest(deviceId = deviceIdA)
            mockMvc.delete("/auth/logout-device") {
                header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessTokenFromB))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
            }

            Then("지정된 기기의 로그인 세션만 삭제된다") {
                // 1. 로그아웃된 device-A의 세션은 삭제되어야 함
                val tokenAuthInfoA = jwtTokenProvider.extractAuthInfo(accessTokenFromA)
                val activeJtiKeyA = getActiveJtiKey(tokenAuthInfoA.jti)
                redisTemplate.opsForValue().get(activeJtiKeyA) shouldBe null

                val userRefreshTokensKey = getRefreshTokenKey(tokenAuthInfoA.role, tokenAuthInfoA.userId)
                redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, deviceIdA) shouldBe null

                val userSessionAgesKey = getSessionKey(tokenAuthInfoA.role, tokenAuthInfoA.userId)
                redisTemplate.opsForZSet().score(userSessionAgesKey, deviceIdA) shouldBe null

                // 2. 요청을 보낸 device-B의 세션은 그대로 남아있어야 함
                redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, deviceIdB) shouldNotBe null

                // 불필요해진 데이터 삭제
                deleteLoginHistory(accessTokenFromB)
            }
        }

        When("모든 기기 로그아웃 요청 시") {
            // 3개의 기기로 로그인 접속
            val deviceIds = listOf("device-1", "device-2", "device-3")
            val accessTokenList = mutableListOf<String>()
            deviceIds.forEach { deviceId ->
                val loginRequest = createLoginRequest(registerMemberDto, deviceId)
                val (accessToken, _) = performLoginAndGetTokens(loginRequest)
                accessTokenList.add(accessToken)
            }

            // When
            mockMvc.delete("/auth/logout-all") {
                header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessTokenList.first()))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.logoutInfos.length()") { value(3) }
                jsonPath("$.data.logoutInfos[*].deviceId", containsInAnyOrder("device-1", "device-2", "device-3"))
            }
            Then("해당 유저의 모든 로그인 세션 정보가 삭제된다") {
                val userRefreshTokensKey = getRefreshTokenKey(Role.MEMBER, testMember.id)
                val userSessionAgesKey = getSessionKey(Role.MEMBER, testMember.id)
                redisTemplate.hasKey(userRefreshTokensKey) shouldBe false
                redisTemplate.hasKey(userSessionAgesKey) shouldBe false
                accessTokenList.forEach { accessToken ->
                    val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(accessToken)
                    val activeJtiKey = getActiveJtiKey(tokenAuthInfo.jti)
                    redisTemplate.opsForValue().get(activeJtiKey) shouldBe null
                }
            }
        }
    }

})


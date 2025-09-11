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
import com.msa.identityservice.infrastructure.IdGenerator
import com.msa.identityservice.jooq.enums.MemberStatus
import com.msa.identityservice.jooq.tables.pojos.Member
import com.msa.identityservice.member.repository.MemberRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.lang.Thread.sleep
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val idGenerator: IdGenerator,
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtProperties: JwtProperties
) {
    lateinit var testMember: Member
    val testPassword = "testPassword1!"

    @BeforeEach
    fun setup() {
        testMember = Member(
            id = idGenerator.generate(),
            email = "test@example.com",
            password = passwordEncoder.encode(testPassword),
            status = MemberStatus.ACTIVE,
        )
        memberRepository.insert(testMember)
    }

    @Test
    fun `정상적인 정보로 로그인 요청 시, 토큰이 발급되고 세션 정보가 Redis에 저장된다`() {
        // Given
        val deviceId = "test-device-1"
        val request = LoginRequest(
            email = testMember.email,
            password = testPassword,
            role = Role.MEMBER,
            deviceId = deviceId
        )

        // When
        val result = mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNoContent() } // 204 No Content
            header { exists(AUTH_HEADER_NAME) }
            cookie { exists(REFRESH_COOKIE_NAME) }
        }.andReturn()

        // Then: Token 검증 및 Redis에 데이터가 올바르게 저장되었는지 직접 검증
        val accessToken = result.response.getHeader(AUTH_HEADER_NAME)!!.substring(AUTH_HEADER_PREFIX.length)
        val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(accessToken)
        tokenAuthInfo.userId shouldBe testMember.id
        tokenAuthInfo.role shouldBe Role.MEMBER
        tokenAuthInfo.deviceId shouldBe deviceId
        tokenAuthInfo.email shouldBe testMember.email

        val userRefreshTokensKey = getRefreshTokenKey(Role.MEMBER, testMember.id)
        val savedRefreshTokenInfo = redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, deviceId)
        savedRefreshTokenInfo shouldNotBe null

        val activeJtiKey = getActiveJtiKey(tokenAuthInfo.jti)
        val savedActiveJtiValue = redisTemplate.opsForValue().get(activeJtiKey)
        savedActiveJtiValue shouldBe testMember.id.toString()

        // 불필요해진 데이터 삭제
        deleteLoginHistory(accessToken)
    }

    private fun deleteLoginHistory(accessToken: String) {
        val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(accessToken)
        val activeJtiKey = getActiveJtiKey(tokenAuthInfo.jti)
        redisTemplate.delete(activeJtiKey)
        val userRefreshTokensKey = getRefreshTokenKey(tokenAuthInfo.role, tokenAuthInfo.userId)
        redisTemplate.opsForHash<String, String>().delete(userRefreshTokensKey, tokenAuthInfo.deviceId)
        val sessionKey = getSessionKey(tokenAuthInfo.role, tokenAuthInfo.userId)
        redisTemplate.opsForZSet().remove(sessionKey, tokenAuthInfo.deviceId)
    }

    @Test
    fun `최대 접속 기기 수를 초과하여 로그인 시, 가장 오래된 세션이 자동 로그아웃된다`() {
        // Given: 최대 접속 기기 수만큼 미리 로그인을 수행해 둠
        val maxDevices = jwtProperties.maxDeviceCount
        val accessTokenList = mutableListOf<String>()
        for (i in 1..maxDevices) {
            sleep(100)
            accessTokenList.add(performLoginAndGetTokens("device-$i").first)
        }

        val userRefreshTokensKey = getRefreshTokenKey(Role.MEMBER, testMember.id)
        redisTemplate.opsForHash<String, String>().size(userRefreshTokensKey) shouldBe maxDevices

        // When: 새로운 기기(최대 수를 초과하는)가 로그인을 시도
        val newDeviceId = "device-${maxDevices + 1}"
        val request = LoginRequest(testMember.email, testPassword, Role.MEMBER, newDeviceId)

        val result = mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNoContent() } // 로그인은 성공해야 함
        }.andReturn()

        // Then: Redis 상태를 검증
        // 1. 전체 세션 수는 여전히 최대 기기 수와 같아야 함
        redisTemplate.opsForHash<String, String>().size(userRefreshTokensKey) shouldBe maxDevices
        // 2. 가장 오래된 세션(device-1)은 삭제되어야 함
        redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, "device-1") shouldBe null
        // 3. 새로 로그인한 세션(newDeviceId)은 존재해야 함
        redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, newDeviceId) shouldNotBe null

        // 불필요해진 데이터 삭제
        accessTokenList.forEach { accessToken -> deleteLoginHistory(accessToken) }
        val accessToken = result.response.getHeader(AUTH_HEADER_NAME)!!.substring(AUTH_HEADER_PREFIX.length)
        deleteLoginHistory(accessToken)
    }

    // 테스트 코드 중복을 줄이기 위한 헬퍼 함수
    private fun performLoginAndGetTokens(deviceId: String): Pair<String, Cookie> {
        val result = performLogin(deviceId).andReturn()
        val accessToken = result.response.getHeader("Authorization")!!.substring(7)
        val refreshTokenCookie = result.response.getCookie("refreshToken")!!
        return Pair(accessToken, refreshTokenCookie)
    }

    private fun performLogin(deviceId: String) = mockMvc.post("/auth/login") {
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(
            LoginRequest(testMember.email, testPassword, Role.MEMBER, deviceId)
        )
    }


    @Test
    fun `유효한 리프레시 토큰으로 토큰 재발급 요청 시, 새로운 토큰들이 발급된다`() {
        // Given: 먼저 로그인을 수행하여 유효한 토큰들을 발급받음
        val deviceId = "test-device-2"
        val (accessToken, refreshTokenCookie) = performLoginAndGetTokens(deviceId)

        // When
        val result = mockMvc.get("/auth/refresh") {
            cookie(refreshTokenCookie)
            header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessToken)) // 재발급 시에도 만료된 AccessToken은 보내야 함
        }.andExpect {
            status { isNoContent() }
        }.andReturn()

        // Then
        val newAccessToken = result.response.getHeader(AUTH_HEADER_NAME)!!.substring(AUTH_HEADER_PREFIX.length)
        val newRefreshTokenCookie = result.response.getCookie(REFRESH_COOKIE_NAME)

        newAccessToken shouldNotBe accessToken
        newRefreshTokenCookie?.value shouldNotBe refreshTokenCookie.value

        // 불필요해진 데이터 삭제
        deleteLoginHistory(newAccessToken)
    }

    @Test
    fun `유효하지 않은(위변조된) 리프레시 토큰으로 재발급 요청 시, 401 에러를 반환한다`() {
        // Given
        val deviceId = "test-device-4"
        val (accessToken, _) = performLoginAndGetTokens(deviceId)

        // 위변조된 가짜 리프레시 토큰 생성
        val fakeRefreshTokenCookie = Cookie("refreshToken", "this.is.fake-token")

        // When & Then
        mockMvc.get("/auth/refresh") {
            cookie(fakeRefreshTokenCookie)
            header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessToken))
        }.andExpect {
            status { isUnauthorized() } // 401 Unauthorized
        }

        // 불필요해진 데이터 삭제
        deleteLoginHistory(accessToken)
    }

    @Test
    fun `이미 사용된(회전된) 리프레시 토큰으로 재발급 요청 시, 401 에러를 반환한다`() {
        // Given
        val deviceId = "test-device-5"
        // 1. 최초 로그인
        val (accessToken_A, refreshTokenCookie_A) = performLoginAndGetTokens(deviceId)

        // 2. 정상적인 토큰 재발급. 이 시점에 refreshToken_A는 무효화됨.
        val result = mockMvc.get("/auth/refresh") {
            cookie(refreshTokenCookie_A)
            header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessToken_A))
        }.andExpect {
            status { isNoContent() }
        }.andReturn()

        // When: 이전에 사용했던 refreshToken_A로 다시 재발급을 시도 (Replay Attack)
        mockMvc.get("/auth/refresh") {
            cookie(refreshTokenCookie_A)
            header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessToken_A))
        }.andExpect {
            // Then: 401 에러가 발생해야 함
            status { isUnauthorized() }
        }

        // 불필요해진 데이터 삭제
        val accessToken = result.response.getHeader(AUTH_HEADER_NAME)!!.substring(AUTH_HEADER_PREFIX.length)
        deleteLoginHistory(accessToken)
    }

    @Test
    fun `유효한 액세스 토큰으로 내 정보 조회 요청 시, 사용자 정보가 반환된다`() {
        // Given
        val deviceId = "test-device-3"
        val (accessToken, _) = performLoginAndGetTokens(deviceId)

        // When & Then
        mockMvc.get("/auth/me") {
            header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessToken))
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.userId") { value(testMember.id) }
            jsonPath("$.data.email") { value(testMember.email) }
            jsonPath("$.data.role") { value(Role.MEMBER.name) }
            jsonPath("$.data.deviceId") { value(deviceId) }
        }

        // 불필요해진 데이터 삭제
        deleteLoginHistory(accessToken)
    }

    @Test
    fun `로그인된 사용자가 자신의 활성 세션 목록을 조회할 수 있다`() {
        // Given: 여러 기기에서 로그인하여 2개의 세션을 생성
        val deviceIdA = "test-device-A"
        val deviceIdB = "test-device-B"
        val (accessTokenFromA, _) = performLoginAndGetTokens(deviceIdA)
        val (accessTokenFromB, _) = performLoginAndGetTokens(deviceIdB)

        // When & Then
        mockMvc.get("/auth/sessions") {
            header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessTokenFromB))
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            // 응답된 세션 목록의 개수가 2개인지 확인
            jsonPath("$.data.sessions.length()") { value(2) }
            // 응답된 세션 목록에 device-A와 device-B가 모두 포함되어 있는지 확인 (순서 무관)
            jsonPath("$.data.sessions[*].deviceId", containsInAnyOrder(deviceIdA, deviceIdB))
        }

        // 불필요해진 데이터 삭제
        deleteLoginHistory(accessTokenFromA)
        deleteLoginHistory(accessTokenFromB)
    }

    @Test
    fun `현재 기기 로그아웃 요청 시, 해당 세션 정보가 삭제되고 쿠키가 초기화된다`() {
        // Given: 'device-A'로 로그인하여 세션을 생성
        val deviceId = "device-A"
        val (accessToken, refreshTokenCookie) = performLoginAndGetTokens(deviceId)
        val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(accessToken)
        val userRefreshTokensKey = getRefreshTokenKey(tokenAuthInfo.role, tokenAuthInfo.userId)
        val userSessionAgesKey = getSessionKey(tokenAuthInfo.role, tokenAuthInfo.userId)

        // 로그인 직후 Redis에 세션 정보가 있는지 확인
        redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, tokenAuthInfo.deviceId) shouldNotBe null
        redisTemplate.opsForZSet().score(userSessionAgesKey, tokenAuthInfo.deviceId) shouldNotBe null

        // When
        mockMvc.delete("/auth/logout") {
            header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessToken))
            cookie(refreshTokenCookie) // 로그아웃 시에도 본인 확인을 위해 쿠키는 필요
        }.andExpect {
            // Then
            status { isOk() }
            // 쿠키가 삭제되었는지 확인 (Max-Age=0)
            cookie {
                maxAge(REFRESH_COOKIE_NAME, 0)
            }
        }

        // Then 2: Redis에서 세션 정보가 모두 삭제되었는지 검증
        val activeJtiKey = getActiveJtiKey(tokenAuthInfo.jti)
        redisTemplate.opsForValue().get(activeJtiKey) shouldBe null
        redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, tokenAuthInfo.deviceId) shouldBe null
        redisTemplate.opsForZSet().score(userSessionAgesKey, tokenAuthInfo.deviceId) shouldBe null
    }

    @Test
    fun `다른 기기 원격 로그아웃 요청 시, 지정된 기기의 세션만 삭제된다`() {
        // Given: device-A와 device-B, 두 개의 세션을 생성
        val deviceIdToLogout = "device-A"
        performLogin(deviceIdToLogout)

        val actingDeviceId = "device-B"
        val (accessTokenFromB, _) = performLoginAndGetTokens(actingDeviceId)

        val userRefreshTokensKey = getRefreshTokenKey(Role.MEMBER, testMember.id)

        // When: device-B에서 device-A를 로그아웃 시킴
        val request = LogoutRequest(deviceId = deviceIdToLogout)
        mockMvc.delete("/auth/logout-device") {
            header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessTokenFromB))
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.deviceId") { value(deviceIdToLogout) }
        }

        // Then: Redis 상태를 검증
        // 1. 로그아웃된 device-A의 세션은 삭제되어야 함
        redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, deviceIdToLogout) shouldBe null
        // 2. 요청을 보낸 device-B의 세션은 그대로 남아있어야 함
        redisTemplate.opsForHash<String, String>().get(userRefreshTokensKey, actingDeviceId) shouldNotBe null

        // 불필요해진 데이터 삭제
        deleteLoginHistory(accessTokenFromB)
    }

    @Test
    fun `모든 기기 로그아웃 요청 시, 해당 유저의 모든 세션 정보가 삭제된다`() {
        // Given: 3개의 다른 기기에서 로그인
        val deviceIds = listOf("device-1", "device-2", "device-3")
        val accessTokenList = mutableListOf<String>()
        deviceIds.forEach { deviceId ->
            val (accessToken, _) = performLoginAndGetTokens(deviceId)
            accessTokenList.add(accessToken)
        }
        val userRefreshTokensKey = getRefreshTokenKey(Role.MEMBER, testMember.id)
        val userSessionAgesKey = getSessionKey(Role.MEMBER, testMember.id)
        redisTemplate.opsForHash<String, String>().size(userRefreshTokensKey) shouldBe 3

        // When
        mockMvc.delete("/auth/logout-all") {
            header(AUTH_HEADER_NAME, getAccessTokenHeaderValue(accessTokenList.first()))
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.logoutInfos.length()") { value(3) }
            jsonPath("$.data.logoutInfos[*].deviceId", containsInAnyOrder("device-1", "device-2", "device-3"))
        }

        // Then: Redis의 모든 관련 키가 삭제되었는지 확인
        redisTemplate.hasKey(userRefreshTokensKey) shouldBe false
        redisTemplate.hasKey(userSessionAgesKey) shouldBe false
        accessTokenList.forEach { accessToken ->
            val tokenAuthInfo = jwtTokenProvider.extractAuthInfo(accessToken)
            val activeJtiKey = getActiveJtiKey(tokenAuthInfo.jti)
            redisTemplate.hasKey(activeJtiKey) shouldBe false
        }
    }
}

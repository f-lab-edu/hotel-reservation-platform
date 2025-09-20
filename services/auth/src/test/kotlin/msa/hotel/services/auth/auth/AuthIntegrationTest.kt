package msa.hotel.services.auth.auth

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.servlet.http.Cookie
import msa.hotel.modules.jwt.token.JwtDecoder
import msa.hotel.services.auth.application.identity.IdentityService
import msa.hotel.services.auth.application.identity.command.RegisterIdentityCommand
import msa.hotel.services.auth.domain.auth.policy.MaximumLoginClientPolicy.MAX_CLIENT
import msa.hotel.services.auth.domain.auth.port.AuthTokenRepository
import msa.hotel.services.auth.domain.identity.model.Role
import msa.hotel.services.auth.infrastructure.persistence.redis.key.AuthTokenKey.makeRefreshTokenKey
import msa.hotel.services.auth.infrastructure.web.auth.dto.LoginRequest
import msa.hotel.services.auth.infrastructure.web.auth.dto.LogoutRequest
import msa.hotel.services.auth.infrastructure.web.auth.header.HeaderConstants.T_ACCESS_HEADER_NAME
import msa.hotel.services.auth.infrastructure.web.auth.header.HeaderConstants.T_ACCESS_HEADER_PREFIX
import msa.hotel.services.auth.infrastructure.web.auth.header.HeaderConstants.T_REFRESH_COOKIE_NAME
import msa.hotel.services.auth.infrastructure.web.auth.header.makeAccessTokenHeaderValue
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.lang.Thread.sleep
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthIntegrationTest(
    private val identityService: IdentityService,
    private val repo: AuthTokenRepository,
    private val mockMvc: MockMvc,
    private val om: ObjectMapper,
    private val jwtDecoder: JwtDecoder,
    private val redisTemplate: RedisTemplate<String, String>,
) : BehaviorSpec({
        fun performLoginAndGetTokens(request: LoginRequest): Pair<String, Cookie> {
            val result =
                mockMvc
                    .post("/auth/login") {
                        contentType = MediaType.APPLICATION_JSON
                        content = om.writeValueAsString(request)
                    }.andReturn()

            val accessToken = result.response.getHeader(T_ACCESS_HEADER_NAME)!!.substring(7)
            val refreshTokenCookie = result.response.getCookie(T_REFRESH_COOKIE_NAME)!!
            return Pair(accessToken, refreshTokenCookie)
        }

        fun createLoginRequest(
            command: RegisterIdentityCommand,
            deviceId: String,
        ) = LoginRequest(
            email = command.email,
            password = command.password,
            role = command.role,
            deviceId = deviceId,
        )

        Given("가입된 멤버 및 로그인 디바이스 정보") {
            val registerCommand =
                RegisterIdentityCommand(
                    email = "${UUID.randomUUID()}@email.com",
                    password = "test1234!",
                    role = Role.MEMBER,
                )
            val registeredIdentity = identityService.register(registerCommand)
            val loginDeviceId = "test-device-1"

            When("동시 접속 기기가 없을 경우의 로그인 API 요청") {
                val request = createLoginRequest(registerCommand, loginDeviceId)

                val result =
                    mockMvc
                        .post("/auth/login") {
                            contentType = MediaType.APPLICATION_JSON
                            content = om.writeValueAsString(request)
                        }.andExpect {
                            status { isCreated() }
                        }.andReturn()

                Then("인증 토큰 발급 및 리프레시 토큰 정보 Redis 저장") {
                    // AccessToken Header 발급 확인
                    val accessTokenHeader = result.response.getHeader(T_ACCESS_HEADER_NAME)
                    accessTokenHeader shouldNotBe null

                    // RefreshToken Cookie 발급 확인
                    val refreshTokenCookie = result.response.getCookie(T_REFRESH_COOKIE_NAME)
                    refreshTokenCookie shouldNotBe null

                    // 발급된 토큰 유효성 검증
                    val accessToken = accessTokenHeader!!.substring(T_ACCESS_HEADER_PREFIX.length)
                    val tokenAuthInfo = jwtDecoder.extractAuthInfo(accessToken)
                    val userInfo = tokenAuthInfo.tokenUserInfo

                    userInfo.userId shouldBe registeredIdentity.id.value
                    userInfo.role shouldBe registeredIdentity.role.name
                    userInfo.deviceId shouldBe loginDeviceId

                    // 로그인 세션 -> 리프레시 토큰 저장 확인
                    val savedRefreshTokenInfo = repo.findRefreshTokenInfo(userInfo)
                    savedRefreshTokenInfo shouldNotBe null
                }
            }

            When("최대 접속 기기까지 로그인 상태일 때 로그인 API 요청") {
                val maxDeviceCount = MAX_CLIENT.toInt()
                for (i in 1..maxDeviceCount) {
                    sleep(100)
                    val loginRequest = createLoginRequest(registerCommand, "device-$i")
                    performLoginAndGetTokens(loginRequest)
                }

                val newDeviceId = "device-${maxDeviceCount + 1}"
                val exceededLoginRequest = createLoginRequest(registerCommand, newDeviceId)
                performLoginAndGetTokens(exceededLoginRequest)

                Then("제일 오래전에 접속한 기기 세션 자동 로그아웃") {
                    val refreshTokensKey = makeRefreshTokenKey(role = registeredIdentity.role.name, userId = registeredIdentity.id.value)

                    // 1. 로그인 세션 정보 -> 리프레시 토큰 수는 최대 기기 수와 같아야 함
                    val refreshTokenCount = redisTemplate.opsForHash<String, String>().size(refreshTokensKey)
                    refreshTokenCount shouldBe maxDeviceCount

                    // 2. 가장 오래된 세션(device-1) 리프레시 토큰은 삭제되어야 함
                    val oldestRefreshToken = redisTemplate.opsForHash<String, String>().get(refreshTokensKey, "device-1")
                    oldestRefreshToken shouldBe null

                    // 3. 가장 마지막 로그인한 세션(newDeviceId) 토큰은 존재해야 함
                    val lastRefreshToken = redisTemplate.opsForHash<String, String>().get(refreshTokensKey, newDeviceId)
                    lastRefreshToken shouldNotBe null
                }
            }

            When("로그인 이후 인증 토큰 재발급 API 요청") {
                val request = createLoginRequest(registerCommand, loginDeviceId)
                val (pastAccessToken, pastRefreshTokenCookie) = performLoginAndGetTokens(request)
                val pastRefreshToken = pastRefreshTokenCookie.value

                val result =
                    mockMvc
                        .post("/auth/reissue") {
                            cookie(pastRefreshTokenCookie)
                            header(T_ACCESS_HEADER_NAME, makeAccessTokenHeaderValue(pastAccessToken)) // 재발급 시에도 AccessToken 확인
                        }.andExpect {
                            status { isCreated() }
                        }.andReturn()

                Then("인증 토큰 재발급 및 기존 인증 토큰 무효화") {
                    // AccessToken Header 재발급 확인
                    val accessTokenHeader = result.response.getHeader(T_ACCESS_HEADER_NAME)
                    accessTokenHeader shouldNotBe null
                    val reissueAccessToken = accessTokenHeader!!.substring(T_ACCESS_HEADER_PREFIX.length)
                    reissueAccessToken shouldNotBe pastAccessToken

                    // RefreshToken Cookie 재발급 확인
                    val refreshTokenCookie = result.response.getCookie(T_REFRESH_COOKIE_NAME)
                    refreshTokenCookie shouldNotBe null
                    val reissueRefreshToken = refreshTokenCookie?.value
                    reissueRefreshToken shouldNotBe pastRefreshToken

                    // 재발급된 리프레시 토큰 저장 확인 & 기존 인증 토큰 무효화 확인
                    val tokenAuthInfo = jwtDecoder.extractAuthInfo(pastAccessToken)
                    val savedRefreshTokenInfo = repo.findRefreshTokenInfo(tokenAuthInfo.tokenUserInfo)
                    savedRefreshTokenInfo shouldNotBe null
                    val storedRefreshToken = savedRefreshTokenInfo!!.token
                    storedRefreshToken shouldNotBe pastRefreshToken

                    // 재발급된 토큰 유효성 검증
                    storedRefreshToken shouldBe reissueRefreshToken
                    val reissueTokenAuthInfo = jwtDecoder.extractAuthInfo(reissueAccessToken)
                    val reissueUserInfo = reissueTokenAuthInfo.tokenUserInfo
                    reissueUserInfo.userId shouldBe registeredIdentity.id.value
                    reissueUserInfo.role shouldBe registeredIdentity.role.name
                    reissueUserInfo.deviceId shouldBe loginDeviceId
                }
            }

            When("로그인 이후 로그아웃 API 요청") {
                val request = createLoginRequest(registerCommand, loginDeviceId)
                val (accessToken, refreshTokenCookie) = performLoginAndGetTokens(request)

                val result =
                    mockMvc
                        .delete("/auth/logout") {
                            cookie(refreshTokenCookie)
                            header(T_ACCESS_HEADER_NAME, makeAccessTokenHeaderValue(accessToken))
                        }.andExpect {
                            status { isOk() }
                        }.andReturn()

                Then("이전에 발급된 AccessToken & RefreshToken 모두 무효화") {
                    // 응답 내역에 리프레시 토큰 쿠키 삭제
                    val refreshTokenCookie = result.response.getCookie(T_REFRESH_COOKIE_NAME)
                    refreshTokenCookie!!.value.isNullOrBlank() shouldBe true

                    // 해당 접속 세션 삭제 -> 리프레시 토큰
                    val tokenAuthInfo = jwtDecoder.extractAuthInfo(accessToken)
                    val savedRefreshTokenInfo = repo.findRefreshTokenInfo(tokenAuthInfo.tokenUserInfo)
                    savedRefreshTokenInfo shouldBe null

                    // 활성 AccessToken 내역 삭제
                    val checkActiveJti = repo.existActiveJti(tokenAuthInfo.jti)
                    checkActiveJti shouldBe false
                }
            }

            When("로그인한 인증 정보로 다른 Device로 접속 세션 로그아웃 API 요청") {
                val loginRequest = createLoginRequest(registerCommand, loginDeviceId)
                val (accessToken, refreshTokenCookie) = performLoginAndGetTokens(loginRequest)

                val anotherDeviceId = "another-device"
                val anotherLoginRequest = createLoginRequest(registerCommand, anotherDeviceId)
                val (anotherAccessToken, anotherRefreshTokenCookie) = performLoginAndGetTokens(anotherLoginRequest)

                val logoutRequest = LogoutRequest(anotherDeviceId)

                val result =
                    mockMvc
                        .delete("/auth/logout-device") {
                            cookie(refreshTokenCookie)
                            header(T_ACCESS_HEADER_NAME, makeAccessTokenHeaderValue(accessToken))
                            content = om.writeValueAsString(logoutRequest)
                            contentType = MediaType.APPLICATION_JSON
                        }.andExpect {
                            status { isOk() }
                        }.andReturn()

                Then("다른 디바이스로 접속한 세션 삭제 -> AccessToken, RefreshToken 즉시 무효화") {
                    val anotherAccessTokenInfo = jwtDecoder.extractAuthInfo(anotherAccessToken)
                    // 리프레시 토큰 내역 삭제 확인
                    val savedAnotherRefreshTokenInfo = repo.findRefreshTokenInfo(anotherAccessTokenInfo.tokenUserInfo)
                    savedAnotherRefreshTokenInfo shouldBe null

                    // 활성 JTI 삭제 확인
                    val checkAnotherActiveJti = repo.existActiveJti(anotherAccessTokenInfo.jti)
                    checkAnotherActiveJti shouldBe false

                    // 다른 접속 중인 세션은 여전히 유효함을 확인
                    val accessTokenInfo = jwtDecoder.extractAuthInfo(accessToken)
                    val savedRefreshTokenInfo = repo.findRefreshTokenInfo(accessTokenInfo.tokenUserInfo)
                    savedRefreshTokenInfo shouldNotBe null

                    val checkActiveJti = repo.existActiveJti(accessTokenInfo.jti)
                    checkActiveJti shouldBe true
                }
            }
        }
    }) {
    // MockMvc는 기본적으로 같은 스레드에서 동기 실행되므로 테스트 트랜잭션에 참여함
    // Kotest BehaviorSpec에서 각 Then은 독립 테스트(leaf)지만,
    // 본 스펙은 첫 번째 요청의 DB 변경을 두 번째 요청 검증에서도 활용하는 시나리오형 흐름을 의도함.
    // SpringTestLifecycleMode.Root를 사용해 스펙 전체를 하나의 Spring TestContext/트랜잭션으로 유지하여
    // 각 Then 사이에 상태를 공유하고, 스펙 종료 시점에 한 번에 롤백되도록 한다.
    override fun extensions() = listOf(SpringTestExtension(SpringTestLifecycleMode.Root))
}

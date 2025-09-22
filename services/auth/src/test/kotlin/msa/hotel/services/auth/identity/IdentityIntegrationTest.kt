package msa.hotel.services.auth.identity

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import msa.hotel.modules.web.response.Response
import msa.hotel.modules.web.support.toResponse
import msa.hotel.services.auth.application.identity.IdentityService
import msa.hotel.services.auth.application.identity.command.RegisterIdentityCommand
import msa.hotel.services.auth.auth.createLoginRequest
import msa.hotel.services.auth.auth.performLoginAndGetTokens
import msa.hotel.services.auth.domain.identity.model.IdentityId
import msa.hotel.services.auth.domain.identity.model.Role
import msa.hotel.services.auth.domain.identity.model.Status
import msa.hotel.services.auth.domain.identity.port.EmailVerifyCodeRepository
import msa.hotel.services.auth.domain.identity.port.IdentityRepository
import msa.hotel.services.auth.infrastructure.web.auth.header.HeaderConstants.T_ACCESS_HEADER_NAME
import msa.hotel.services.auth.infrastructure.web.auth.header.makeAccessTokenHeaderValue
import msa.hotel.services.auth.infrastructure.web.identity.dto.PasswordChangeRequest
import msa.hotel.services.auth.infrastructure.web.identity.dto.PasswordChangeResponse
import msa.hotel.services.auth.infrastructure.web.identity.dto.RegisterIdentityRequest
import msa.hotel.services.auth.infrastructure.web.identity.dto.RegisterIdentityResponse
import msa.hotel.services.auth.infrastructure.web.identity.dto.SendVerifyEmailRequest
import msa.hotel.services.auth.infrastructure.web.identity.dto.SendVerifyEmailResponse
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class IdentityIntegrationTest(
    private val mockMvc: MockMvc,
    private val om: ObjectMapper,
    private val repo: IdentityRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailVerifyCodeRepo: EmailVerifyCodeRepository,
    private val identityService: IdentityService,
) : BehaviorSpec({
        Given("정상적인 신규 Identity 등록 정보") {
            val request =
                RegisterIdentityRequest(
                    email = "${UUID.randomUUID()}@email.com",
                    password = "password1234!",
                    role = Role.MEMBER,
                )

            When("처음 등록('register') API 요청") {
                val apiResult =
                    mockMvc
                        .post("/identity") {
                            contentType = MediaType.APPLICATION_JSON
                            content = om.writeValueAsString(request)
                        }.andExpect {
                            status { isCreated() }
                        }.andReturn()

                Then("성공 응답 및 신규 등록 정보 DB에 저장(비밀번호 암호화)") {
                    val response: Response<RegisterIdentityResponse> = apiResult.toResponse(om)
                    response.success shouldBe true
                    response.code shouldBe "CREATED"
                    response.message shouldBe "Identity 정보를 등록 했습니다."

                    val data = response.data!!
                    val identityId = IdentityId(data.id)
                    val identity = repo.findById(identityId)
                    identity shouldNotBe null
                    identity!!.status shouldBe Status.PENDING
                    data.email shouldBe request.email
                    data.email shouldBe identity!!.email
                    data.role shouldBe request.role!!.name
                    data.role shouldBe identity.role.name

                    passwordEncoder.matches(request.password!!, identity.passwordHash) shouldBe true
                }
            }

            When("이미 등록된 이후 등록('register') API 재요청") {
                val apiResult =
                    mockMvc
                        .post("/identity") {
                            contentType = MediaType.APPLICATION_JSON
                            content = om.writeValueAsString(request)
                        }.andExpect {
                            status { isConflict() }
                        }.andReturn()

                Then("중복 이메일 정보로 Identity 등록 실패 응답") {
                    val response: Response<RegisterIdentityResponse> = apiResult.toResponse(om)
                    response.success shouldBe false
                    response.code shouldBe "CONFLICT"
                    response.message shouldBe "이미 사용 중인 이메일입니다. 다른 이메일로 등록해주세요."
                }
            }

            var userId: ULong? = null
            var code: String? = null
            When("가입된 정보로 이메일 검증 API 요청") {
                val sendVerifyEmailRequest = SendVerifyEmailRequest(request.email)
                val apiResult =
                    mockMvc
                        .post("/identity/verify/request") {
                            contentType = MediaType.APPLICATION_JSON
                            content = om.writeValueAsString(sendVerifyEmailRequest)
                        }.andExpect {
                            status { isOk() }
                        }.andReturn()

                Then("이메일 인증 코드 저장") {
                    val response: Response<SendVerifyEmailResponse> = apiResult.toResponse(om)
                    response.message shouldBe "${sendVerifyEmailRequest.email} 메일 발송 처리 됐습니다."
                    userId = response.data!!.id
                    // 이메일 인증 코드 redis 저장
                    code = emailVerifyCodeRepo.findCodeByUserId(userId)
                    code shouldNotBe null
                }
            }

            When("이메일 검증 메일 발송 처리 후 이메일 검증 확인 API 요청") {

                mockMvc
                    .get("/identity/verify/confirm?code=$code&email=${request.email}")
                    .andExpect {
                        status { isOk() }
                    }.andReturn()

                Then("identity 상태 ACTIVE로 업데이트, 이메일 인증 코드 redis 삭제") {
                    val identity = repo.findById(IdentityId(userId!!))
                    identity!!.status shouldBe Status.ACTIVE

                    // 이메일 인증 코드 redis 삭제됨
                    code = emailVerifyCodeRepo.findCodeByUserId(userId)
                    code shouldBe null
                }
            }
        }

        Given("이메일 검증까지 완료된 후 로그인 완료") {
            val registerCommand =
                RegisterIdentityCommand(
                    email = "${UUID.randomUUID()}@email.com",
                    password = "test1234!",
                    role = Role.MEMBER,
                )
            val identityDto = identityService.register(registerCommand)
            val identity = repo.findById(IdentityId(identityDto.id.value))!!
            identity.verifyMail()
            repo.save(identity)

            val loginRequest = createLoginRequest(registerCommand, "test-device-1")
            val (accessToken, refreshTokenCookie) = performLoginAndGetTokens(loginRequest, mockMvc, om)

            When("로그인된 인증 정보로 비밀번호 변경 API 요청") {
                val request =
                    PasswordChangeRequest(
                        currentPassword = registerCommand.password,
                        changePassword = "1234test!",
                    )
                val apiResult =
                    mockMvc
                        .post("/identity/password/change") {
                            contentType = MediaType.APPLICATION_JSON
                            content = om.writeValueAsString(request)
                            cookie(refreshTokenCookie)
                            header(T_ACCESS_HEADER_NAME, makeAccessTokenHeaderValue(accessToken))
                        }.andExpect {
                            status { isOk() }
                        }.andReturn()
                Then("비밀번호 변경 확인") {
                    val response: Response<PasswordChangeResponse> = apiResult.toResponse(om)
                    response.message shouldBe "비밀번호 변경에 성공했습니다."
                    val data = response.data!!
                    data.id shouldBe identity.id.value
                    data.email shouldBe identity.email
                    data.role shouldBe identity.role.name

                    val savedIdentity = repo.findById(IdentityId(identity.id.value))!!
                    passwordEncoder.matches(request.changePassword!!, savedIdentity.passwordHash) shouldBe true
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

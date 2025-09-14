package com.msa.accommodationservice.accommodation

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.accommodationservice.accommodation.controller.request.AccommodationRegistrationRequest
import com.msa.accommodationservice.accommodation.controller.request.AccommodationUpdateRequest
import com.msa.accommodationservice.accommodation.repository.AccommodationRepository
import com.msa.supportmodule.auth.consts.AuthConstants.ROLE_HEADER_NAME
import com.msa.supportmodule.auth.consts.AuthConstants.USER_ID_HEADER_NAME
import com.msa.supportmodule.auth.token.enums.Role
import com.msa.supportmodule.exception.BusinessErrorCode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccommodationIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val accommodationRepository: AccommodationRepository,
) : BehaviorSpec({

    Given("신규 숙소 정보 등록") {
        val role = Role.HOST
        val hostId = 1L

        val request = AccommodationRegistrationRequest(
            name = "테스트 숙소 ${UUID.randomUUID()}",
            description = "테스트 숙소 설명",
            contactNumber = "010-1234-5678",
            city = "서울",
            address = "서울 강남구"
        )

        When("숙소 정보 등록 API 요청") {
            val result = mockMvc.post("/accommodations") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
                header(ROLE_HEADER_NAME, role.name)
                header(USER_ID_HEADER_NAME, hostId.toString())
            }.andExpect {
                status { isCreated() } // 201 Created 상태인지 확인
                jsonPath("$.success") { value(true) }
                jsonPath("$.code") { value("create") }
                jsonPath("$.message") { value("숙소 정보를 등록 했습니다.") }
            }.andReturn()

            Then("숙소 정보 DB에 저장") {
                val responseBody = objectMapper.readTree(result.response.contentAsString)
                val accommodationId = responseBody.get("data").get("accommodationId").asLong()
                val savedAccommodation = accommodationRepository.findById(accommodationId)
                savedAccommodation shouldNotBe null
                savedAccommodation?.name shouldBe request.name
                savedAccommodation?.description shouldBe request.description
                savedAccommodation?.contactNumber shouldBe request.contactNumber
                savedAccommodation?.city shouldBe request.city
                savedAccommodation?.address shouldBe request.address
            }
        }

        When("같은 호스트 계정이 이미 숙소 정보를 등록한 이후 숙소 정보 등록 API 요청") {
            val result = mockMvc.post("/accommodations") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
                header(ROLE_HEADER_NAME, role.name)
                header(USER_ID_HEADER_NAME, hostId.toString())
            }.andExpect {
                status { isConflict() } // 409 Conflict 상태인지 확인
                jsonPath("$.success") { value(false) }
                jsonPath("$.code") { value(BusinessErrorCode.CONFLICT.name) }
            }.andReturn()

            Then("이미 숙소 정보를 등록한 호스트 계정으로 등록 실패 확인") {
                val responseBody = objectMapper.readTree(result.response.contentAsString)
                val message = responseBody.get("message").asText()
                message shouldBe "이미 숙소 정보를 등록했습니다. 수정을 원할시 수정 기능을 사용하세요."
            }
        }

        When("이미 등록된 숙소 이름과 주소가 같은 정보로 다른 계정이 숙소 정보 등록 API 요청") {
            val anotherHostId = 2L

            val result = mockMvc.post("/accommodations") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
                header(ROLE_HEADER_NAME, role.name)
                header(USER_ID_HEADER_NAME, anotherHostId.toString())
            }.andExpect {
                status { isConflict() } // 409 Conflict 상태인지 확인
                jsonPath("$.success") { value(false) }
                jsonPath("$.code") { value(BusinessErrorCode.CONFLICT.name) }
            }.andReturn()

            Then("이미 등록된 숙소 이름와 주소로 등록 실패 확인") {
                val responseBody = objectMapper.readTree(result.response.contentAsString)
                val message = responseBody.get("message").asText()
                message shouldBe "이미 해당 이름과 주소의 숙소 정보가 존재합니다."
            }
        }
    }

    Given("숙소 정보 수정") {
        val role = Role.HOST
        val hostId = 1L

        val registrationRequest = AccommodationRegistrationRequest(
            name = "테스트 숙소 ${UUID.randomUUID()}",
            description = "테스트 숙소 설명",
            contactNumber = "010-1234-5678",
            city = "서울",
            address = "서울 강남구"
        )

        When("숙소 정보 수정 API 요청") {
            val registrationResult = mockMvc.post("/accommodations") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(registrationRequest)
                header(ROLE_HEADER_NAME, role.name)
                header(USER_ID_HEADER_NAME, hostId.toString())
            }.andReturn()

            val responseBody = objectMapper.readTree(registrationResult.response.contentAsString)
            val accommodationId = responseBody.get("data").get("accommodationId").asLong()

            val updateRequest = AccommodationUpdateRequest(
                name = "수정된 숙소 이름",
                description = "수정된 숙소 설명",
                contactNumber = "010-9876-5432",
                city = "부산",
                address = "부산 해운대구"
            )

            val result = mockMvc.patch("/accommodations") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(updateRequest)
                header(ROLE_HEADER_NAME, role.name)
                header(USER_ID_HEADER_NAME, hostId.toString())
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.code") { value("update") }
                jsonPath("$.message") { value("숙소 정보를 수정 했습니다.") }
            }.andReturn()

            Then("숙소 정보 업데이트 확인") {
                val updatedAccommodation = accommodationRepository.findById(accommodationId)
                updatedAccommodation shouldNotBe null
                updatedAccommodation?.name shouldBe updateRequest.name
                updatedAccommodation?.description shouldBe updateRequest.description
                updatedAccommodation?.contactNumber shouldBe updateRequest.contactNumber
                updatedAccommodation?.city shouldBe updateRequest.city
                updatedAccommodation?.address shouldBe updateRequest.address
            }
        }
    }

    Given("숙소 리스트 조회") {
        val role = Role.HOST
        val hostId = 1L

        val request = AccommodationRegistrationRequest(
            name = "테스트 숙소 ${UUID.randomUUID()}",
            description = "테스트 숙소 설명",
            contactNumber = "010-1234-5678",
            city = "서울",
            address = "서울 강남구"
        )

        When("숙소 리스트 조회 API 요청") {
            mockMvc.post("/accommodations") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
                header(ROLE_HEADER_NAME, role.name)
                header(USER_ID_HEADER_NAME, hostId.toString())
            }.andReturn()

            val result = mockMvc.get("/accommodations") {
                header(ROLE_HEADER_NAME, role.name)
                header(USER_ID_HEADER_NAME, hostId.toString())
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.code") { value("read") }
                jsonPath("$.message") { value("숙소 리스트 조회에 성공 했습니다.") }
            }.andReturn()

            Then("숙소 리스트 조회 성공") {
                val responseBody = objectMapper.readTree(result.response.contentAsString)
                val accommodations = responseBody.get("data").get("accommodations")
                accommodations.isArray shouldBe true
                accommodations.size() shouldBe 1
            }
        }
    }

    Given("특정 숙소 정보 조회") {
        val role = Role.HOST
        val hostId = 1L

        val request = AccommodationRegistrationRequest(
            name = "테스트 숙소 ${UUID.randomUUID()}",
            description = "테스트 숙소 설명",
            contactNumber = "010-1234-5678",
            city = "서울",
            address = "서울 강남구"
        )

        When("특정 숙소 정보 조회 API 요청") {
            val registrationResult = mockMvc.post("/accommodations") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
                header(ROLE_HEADER_NAME, role.name)
                header(USER_ID_HEADER_NAME, hostId.toString())
            }.andReturn()

            val responseBody = objectMapper.readTree(registrationResult.response.contentAsString)
            val accommodationId = responseBody.get("data").get("accommodationId").asLong()

            val result = mockMvc.get("/accommodations/$accommodationId") {
                header(ROLE_HEADER_NAME, role.name)
                header(USER_ID_HEADER_NAME, hostId.toString())
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.code") { value("read") }
                jsonPath("$.message") { value("숙소 정보를 조회에 성공했습니다.") }
            }.andReturn()

            Then("특정 숙소 정보 조회 성공") {
                val getResponseBody = objectMapper.readTree(result.response.contentAsString)
                val accommodation = getResponseBody.get("data")
                accommodation.get("name").asText() shouldBe request.name
                accommodation.get("description").asText() shouldBe request.description
                accommodation.get("contactNumber").asText() shouldBe request.contactNumber
                accommodation.get("city").asText() shouldBe request.city
                accommodation.get("address").asText() shouldBe request.address
            }
        }
    }

    Given("본인 숙소 정보 조회") {
        val role = Role.HOST
        val hostId = 1L

        val request = AccommodationRegistrationRequest(
            name = "테스트 숙소 ${UUID.randomUUID()}",
            description = "테스트 숙소 설명",
            contactNumber = "010-1234-5678",
            city = "서울",
            address = "서울 강남구"
        )

        When("본인 숙소 정보 조회 API 요청") {
            mockMvc.post("/accommodations") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
                header(ROLE_HEADER_NAME, role.name)
                header(USER_ID_HEADER_NAME, hostId.toString())
            }.andReturn()

            val result = mockMvc.get("/accommodations/me") {
                header(ROLE_HEADER_NAME, role.name)
                header(USER_ID_HEADER_NAME, hostId.toString())
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.code") { value("read") }
                jsonPath("$.message") { value("숙소 정보를 조회에 성공했습니다.") }
            }.andReturn()

            Then("본인 숙소 정보 조회 성공") {
                val responseBody = objectMapper.readTree(result.response.contentAsString)
                val accommodation = responseBody.get("data")
                accommodation.get("name").asText() shouldBe request.name
                accommodation.get("description").asText() shouldBe request.description
                accommodation.get("contactNumber").asText() shouldBe request.contactNumber
                accommodation.get("city").asText() shouldBe request.city
                accommodation.get("address").asText() shouldBe request.address
                accommodation.get("hostId").asLong() shouldBe hostId
            }
        }
    }

})

package com.reservation.admin.terms.controller

import com.reservation.admin.terms.controller.request.*
import com.reservation.admin.terms.repository.dto.SearchTermsResult
import com.reservation.admin.terms.service.TermsService
import com.reservation.domain.terms.Terms
import com.reservation.querysupport.page.KeysetPage
import com.reservation.support.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/terms") // 임시 인증 제외 조치
@RequiredArgsConstructor
@Tag(name = "약관 API", description = "관리자용 약관 관리 API 입니다.")
class TermsController {
    private val termsService: TermsService? = null

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "약관 등록", description = "관리자가 새로운 약관을 등록합니다.")
    fun create(@RequestBody request: CreateTermsRequest): ApiResponse<Long> {
        val requestNewTerms = request.validToTerms()

        val newTermsId = termsService!!.create(requestNewTerms)

        return ApiResponse.ok(newTermsId)
    }

    @PutMapping("{id}")
    @Operation(summary = "약관 수정", description = "관리자가 약관을 수정합니다. 버전이 올라간 새로운 약관이 생성 됩니다 (이력 관리).")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateTermsRequest
    ): ApiResponse<Long> {
        val requestNewVersionTerms = request.validToTerms(id)

        val newVersionTermsId = termsService!!.update(requestNewVersionTerms)

        return ApiResponse.ok(newVersionTermsId)
    }

    @GetMapping("/{id}")
    @Operation(summary = "약관 상세 조회", description = "(조문 포함) 약관 상세 조회합니다.")
    fun findById(@PathVariable id: Long): ApiResponse<Terms> {
        val findTerms = termsService!!.findById(id)

        return ApiResponse.ok(findTerms)
    }

    @PostMapping("/search")
    @Operation(summary = "약관 리스트 검색 (페이지 포맷)", description = "(조문 X) 관리자가 약관 리스트 조회 & 검색합니다.")
    fun searchTermsFormatPage(
        @RequestBody condition: TermsSearchCondition
    ): ApiResponse<Page<SearchTermsResult>> {
        condition.validate()
        val searchCodeOrNull = condition.codeOrNull
        val isLatest = condition.isLatestOrNull == null || condition.isLatestOrNull
        val pageRequest = condition.toPageRequest()

        val searchTermsList = termsService!!.searchTermsFormatPage(searchCodeOrNull, isLatest, pageRequest)

        return ApiResponse.ok(searchTermsList)
    }

    @PostMapping("/search-keyset")
    @Operation(summary = "약관 리스트 검색 [커서 포맷]", description = "(조문 X) 관리자가 약관 리스트 조회 & 검색합니다.")
    fun searchTermsFormatCursor(
        @RequestBody condition: TermsSearchCursorCondition
    ): ApiResponse<KeysetPage<SearchTermsResult, TermsCursor>> {
        val searchCodeOrNull = condition.codeOrNull
        val isLatest = condition.isLatestOrNull == null || condition.isLatestOrNull
        val size = if (condition.size == null) condition.defaultSize() else condition.size
        val cursors = condition.cursors()

        val terms = termsService!!.searchTermsFormatCursor(searchCodeOrNull, isLatest, size, cursors)

        return ApiResponse.ok(terms)
    }
}

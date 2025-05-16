package com.reservation.admin.terms.controller;

import static com.reservation.support.response.ApiResponse.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.admin.terms.controller.request.CreateTermsRequest;
import com.reservation.admin.terms.controller.request.TermsCursor;
import com.reservation.admin.terms.controller.request.TermsSearchCondition;
import com.reservation.admin.terms.controller.request.TermsSearchCursorCondition;
import com.reservation.admin.terms.controller.request.UpdateTermsRequest;
import com.reservation.admin.terms.repository.dto.SearchTermsResult;
import com.reservation.admin.terms.service.TermsService;
import com.reservation.domain.terms.Terms;
import com.reservation.domain.terms.enums.TermsCode;
import com.reservation.querysupport.page.KeysetPage;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/terms") // 임시 인증 제외 조치
@RequiredArgsConstructor
@Tag(name = "약관 API", description = "관리자용 약관 관리 API 입니다.")
public class TermsController {
	private final TermsService termsService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "약관 등록", description = "관리자가 새로운 약관을 등록합니다.")
	public ApiResponse<Long> create(@RequestBody CreateTermsRequest request) {
		Terms requestNewTerms = request.validToTerms();

		Long newTermsId = termsService.create(requestNewTerms);

		return ok(newTermsId);
	}

	@PutMapping("{id}")
	@Operation(summary = "약관 수정", description = "관리자가 약관을 수정합니다. 버전이 올라간 새로운 약관이 생성 됩니다 (이력 관리).")
	public ApiResponse<Long> update(
		@PathVariable long id,
		@RequestBody UpdateTermsRequest request
	) {
		Terms requestNewVersionTerms = request.validToTerms(id);

		Long newVersionTermsId = termsService.update(requestNewVersionTerms);

		return ok(newVersionTermsId);
	}

	@GetMapping("/{id}")
	@Operation(summary = "약관 상세 조회", description = "(조문 포함) 약관 상세 조회합니다.")
	public ApiResponse<Terms> findById(@PathVariable long id) {
		Terms findTerms = termsService.findById(id);

		return ok(findTerms);
	}

	@PostMapping("/search")
	@Operation(summary = "약관 리스트 검색 (페이지 포맷)", description = "(조문 X) 관리자가 약관 리스트 조회 & 검색합니다.")
	public ApiResponse<Page<SearchTermsResult>> searchTermsFormatPage(
		@RequestBody TermsSearchCondition condition
	) {
		condition.validate();
		TermsCode searchCodeOrNull = condition.codeOrNull();
		boolean isLatest = condition.isLatestOrNull() == null || condition.isLatestOrNull();
		PageRequest pageRequest = condition.toPageRequest();

		Page<SearchTermsResult> searchTermsList =
			termsService.searchTermsFormatPage(searchCodeOrNull, isLatest, pageRequest);

		return ok(searchTermsList);
	}

	@PostMapping("/search-keyset")
	@Operation(summary = "약관 리스트 검색 [커서 포맷]", description = "(조문 X) 관리자가 약관 리스트 조회 & 검색합니다.")
	public ApiResponse<KeysetPage<SearchTermsResult, TermsCursor>> searchTermsFormatCursor(
		@RequestBody TermsSearchCursorCondition condition
	) {
		TermsCode searchCodeOrNull = condition.codeOrNull();
		boolean isLatest = condition.isLatestOrNull() == null || condition.isLatestOrNull();
		int size = condition.size() == null ? condition.defaultSize() : condition.size();
		List<TermsCursor> cursors = condition.cursors();

		KeysetPage<SearchTermsResult, TermsCursor> terms =
			termsService.searchTermsFormatCursor(searchCodeOrNull, isLatest, size, cursors);

		return ok(terms);
	}
}

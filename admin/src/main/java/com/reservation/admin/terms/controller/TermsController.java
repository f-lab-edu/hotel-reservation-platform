package com.reservation.admin.terms.controller;

import static com.reservation.common.response.ApiResponse.*;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.admin.terms.controller.dto.request.CreateTermsRequest;
import com.reservation.admin.terms.controller.dto.request.TermsKeysetSearchCondition;
import com.reservation.admin.terms.controller.dto.request.TermsSearchCondition;
import com.reservation.admin.terms.controller.dto.request.UpdateTermsRequest;
import com.reservation.admin.terms.service.TermsService;
import com.reservation.common.response.ApiResponse;
import com.reservation.commonapi.admin.query.cursor.AdminTermsCursor;
import com.reservation.commonapi.admin.repository.dto.AdminTermsDto;
import com.reservation.commonmodel.keyset.KeysetPage;
import com.reservation.commonmodel.terms.TermsDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/terms")
@Tag(name = "약관 API", description = "관리자용 약관 관리 API입니다.")
@RequiredArgsConstructor
public class TermsController {
	private final TermsService termsService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "약관 등록", description = "관리자가 새로운 약관을 등록합니다.")
	public ApiResponse<Long> createTerms(@Valid @RequestBody CreateTermsRequest request) {
		Long termsId = termsService.createTerms(request);
		return ok(termsId);
	}

	@PutMapping
	@Operation(summary = "약관 수정", description = "관리자가 기존 약관을 수정합니다. 버전이 업데이트 됩니다")
	public ApiResponse<Long> updateTerms(@Valid @RequestBody UpdateTermsRequest request) {
		Long termsId = termsService.updateTerms(request);
		return ok(termsId);
	}

	@PostMapping("/search")
	@Operation(summary = "약관 리스트 조회", description = "관리자가 약관 리스트를 조회합니다.")
	public ApiResponse<Page<AdminTermsDto>> findTerms(@Valid @RequestBody TermsSearchCondition condition) {
		Page<AdminTermsDto> terms = termsService.findTerms(condition);
		return ok(terms);
	}

	@PostMapping("/search-keyset")
	@Operation(summary = "약관 리스트 조회 [커서 방식]", description = "관리자가 약관 리스트를 조회합니다.")
	public ApiResponse<KeysetPage<AdminTermsDto, AdminTermsCursor>> findTermsByKeyset(
		@Valid @RequestBody TermsKeysetSearchCondition condition) {
		KeysetPage<AdminTermsDto, AdminTermsCursor> terms = termsService.findTermsByKeyset(condition);
		return ok(terms);
	}

	@GetMapping("/{id}")
	@Operation(summary = "약관 상세 조회", description = "약관 상세 조회합니다.")
	public ApiResponse<TermsDto> findById(@Nonnull @PathVariable Long id) {
		TermsDto findTerms = termsService.findById(id);
		return ok(findTerms);
	}
}

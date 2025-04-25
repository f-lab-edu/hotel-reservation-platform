package com.reservation.customer.terms.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.customer.terms.controller.request.TermsSearchCondition;
import com.reservation.customer.terms.service.TermsService;
import com.reservation.customer.terms.service.dto.SearchTerms;
import com.reservation.domain.terms.Terms;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/no-auth/terms")  //❗JWT auth 제외
@Tag(name = "약관 API", description = "고객용 약관 관리 API 입니다.")
@RequiredArgsConstructor
public class TermsController {
	private final TermsService termsService;

	@PostMapping("/search")
	@Operation(summary = "약관 리스트 조회", description = "최신 버전 & 현재 사용 중인 약관 리스트를 조회합니다.")
	public ApiResponse<Page<SearchTerms>> findTerms(
		@Valid @RequestBody TermsSearchCondition condition
	) {
		Page<SearchTerms> termsPage = termsService.findTerms(condition);

		return ApiResponse.ok(termsPage);
	}

	@GetMapping("/{id}")
	@Operation(summary = "약관 상세 조회", description = "특정 약관의 조문 내용을 확인합니다.")
	public ApiResponse<Terms> findTermsById(@PathVariable long id) {
		Terms findTerms = termsService.findById(id);

		return ApiResponse.ok(findTerms);
	}
}

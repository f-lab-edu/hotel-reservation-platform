package com.reservation.customer.terms.controller;

import static com.reservation.common.response.ApiResponse.*;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.common.response.ApiResponse;
import com.reservation.commonapi.customer.repository.dto.CustomerTermsDto;
import com.reservation.customer.terms.controller.dto.request.TermsSearchCondition;
import com.reservation.customer.terms.service.TermsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/terms")
@Tag(name = "약관 API", description = "고객용 약관 관리 API입니다.")
@RequiredArgsConstructor
public class TermsController {
	private final TermsService termsService;

	@PostMapping("/search")
	@Operation(summary = "약관 리스트 조회", description = "최신 버전 & 현재 사용 중인 약관 리스트를 조회합니다.")
	public ApiResponse<Page<CustomerTermsDto>> findTerms(@Valid @RequestBody TermsSearchCondition condition) {
		Page<CustomerTermsDto> terms = termsService.findTerms(condition);
		return ok(terms);
	}

}

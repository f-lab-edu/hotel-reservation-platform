package com.reservation.admin.terms.controller;

import static com.reservation.common.support.response.ApiResponses.*;
import static com.reservation.common.support.validation.ModelAttributeValidator.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.admin.terms.controller.dto.request.CreateTermsRequest;
import com.reservation.admin.terms.controller.dto.request.TermsSearchCondition;
import com.reservation.admin.terms.controller.dto.request.UpdateTermsRequest;
import com.reservation.admin.terms.service.TermsService;
import com.reservation.common.response.ApiSuccessResponse;
import com.reservation.commonapi.terms.repository.dto.AdminTermsDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/terms")
@Tag(name = "약관 API", description = "관리자용 약관 관리 API입니다.")
@RequiredArgsConstructor
public class TermsController {
	private final TermsService termsService;

	@Operation(summary = "약관 등록", description = "관리자가 새로운 약관을 등록합니다.")
	@ApiResponse(responseCode = "201", description = "등록 성공",
		content = @Content(schema = @Schema(implementation = ApiSuccessResponse.class)))
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<Long>> createTerms(@Valid @RequestBody CreateTermsRequest request) {
		Long termsId = termsService.createTerms(request);
		return created(termsId);
	}

	@Operation(summary = "약관 수정", description = "관리자가 기존 약관을 수정합니다. 버전이 업데이트 됩니다")
	@ApiResponse(responseCode = "200", description = "수정 성공",
		content = @Content(schema = @Schema(implementation = ApiSuccessResponse.class)))
	@PutMapping
	public ResponseEntity<ApiSuccessResponse<Long>> updateTerms(@Valid @RequestBody UpdateTermsRequest request) {
		Long termsId = termsService.updateTerms(request);
		return ok(termsId);
	}

	@Operation(summary = "약관 리스트 조회", description = "관리자가 약관 리스트를 조회합니다.")
	@GetMapping
	public ResponseEntity<ApiSuccessResponse<Page<AdminTermsDto>>> findTerms(
		@ParameterObject @Valid @ModelAttribute TermsSearchCondition condition,
		BindingResult bindingResult
	) {
		validate(bindingResult);

		Page<AdminTermsDto> terms = termsService.findTerms(condition);
		return ok(terms);
	}
}

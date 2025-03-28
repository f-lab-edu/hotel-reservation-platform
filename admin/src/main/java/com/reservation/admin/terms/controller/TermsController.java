package com.reservation.admin.terms.controller;

import static com.reservation.common.support.response.ApiResponses.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.admin.terms.controller.dto.AdminCreateTermsRequest;
import com.reservation.admin.terms.controller.dto.AdminUpdateTermsRequest;
import com.reservation.admin.terms.service.TermsService;
import com.reservation.common.response.ApiSuccessResponse;

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
	@ApiResponse(responseCode = "201", description = "등록 성공", content = @Content(schema = @Schema(implementation = ApiSuccessResponse.class)))
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<Long>> createTerms(@Valid @RequestBody AdminCreateTermsRequest request) {
		Long termsId = termsService.createTerms(request);
		return created(termsId);
	}

	@Operation(summary = "약관 수정", description = "관리자가 기존 약관을 수정합니다. 버전이 업데이트 됩니다")
	@ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = ApiSuccessResponse.class)))
	@PutMapping
	public ResponseEntity<ApiSuccessResponse<Long>> updateTerms(@Valid @RequestBody AdminUpdateTermsRequest request) {
		Long termsId = termsService.updateTerms(request);
		return ok(termsId);
	}
}

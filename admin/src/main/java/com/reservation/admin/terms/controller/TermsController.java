package com.reservation.admin.terms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.reservation.admin.terms.controller.dto.AdminCreateTermsRequest;
import com.reservation.admin.terms.service.TermsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/terms")
@Tag(name = "약관 API", description = "관리자용 약관 관리 API입니다.")
@RequiredArgsConstructor
public class TermsController {
	private final TermsService termsService;

	@Operation(summary = "약관 등록", description = "관리자가 새로운 약관을 등록합니다.")
	@ApiResponse(responseCode = "201", description = "등록 성공", content = @Content(schema = @Schema(implementation = Long.class)))
	@PostMapping
	public ResponseEntity<Long> createTerms(@Valid @RequestBody AdminCreateTermsRequest request) {
		Long termsId = termsService.createTerms(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(termsId);
	}
}

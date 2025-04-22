package com.reservation.host.accommodation.controller;

import static com.reservation.common.response.ApiResponse.*;
import static com.reservation.host.auth.controller.AuthController.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.common.response.ApiResponse;
import com.reservation.commonauth.auth.annotation.LoginUserId;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.host.accommodation.controller.dto.request.CreateAccommodationRequest;
import com.reservation.host.accommodation.controller.dto.request.UpdateAccommodationRequest;
import com.reservation.host.accommodation.service.AccommodationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accommodation")
@Tag(name = "숙소 관리 API", description = "숙박 업체 숙소 관리 API입니다.")
@PreAuthorize(PRE_AUTH_ROLE_HOST)//✅숙박 업체만 접근 가능
@RequiredArgsConstructor
public class AccommodationController {
	private final AccommodationService accommodationService;

	@PostMapping
	@Operation(summary = "숙소 등록", description = "숙박 업체가 숙소를 등록합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Long> createAccommodation(@Valid @RequestBody CreateAccommodationRequest request,
		@LoginUserId Long hostId) {
		Long accommodationId = accommodationService.createAccommodation(request, hostId);
		return ok(accommodationId);
	}

	@PutMapping
	@Operation(summary = "숙소 수정", description = "숙박 업체가 숙소를 수정합니다.")
	public ApiResponse<Long> updateAccommodation(@Valid @RequestBody UpdateAccommodationRequest request,
		@LoginUserId Long hostId) {
		Long accommodationId = accommodationService.updateAccommodation(request, hostId);
		return ok(accommodationId);
	}

	@GetMapping
	@Operation(summary = "숙소 조회", description = "숙박 업체가 숙소를 조회합니다.")
	public ApiResponse<AccommodationDto> findHostAccommodation(@LoginUserId Long hostId) {
		AccommodationDto accommodation = accommodationService.findHostAccommodation(hostId);
		return ok(accommodation);
	}
}

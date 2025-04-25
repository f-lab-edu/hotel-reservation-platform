package com.reservation.host.accommodation.controller;

import static com.reservation.host.auth.controller.AuthController.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.auth.annotation.LoginUserId;
import com.reservation.domain.accommodation.Accommodation;
import com.reservation.host.accommodation.controller.request.NewAccommodationRequest;
import com.reservation.host.accommodation.service.AccommodationService;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accommodation")
@Tag(name = "숙소 관리 API", description = "숙박 업체 숙소 관리 API 입니다.")
@PreAuthorize(PRE_AUTH_ROLE_HOST) //✅숙박 업체만 접근 가능
@RequiredArgsConstructor
public class AccommodationController {
	private final AccommodationService accommodationService;

	@PostMapping
	@Operation(summary = "숙소 등록", description = "숙박 업체가 숙소를 등록합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Long> createAccommodation(
		@RequestBody NewAccommodationRequest request,
		@LoginUserId long hostId
	) {
		long accommodationId =
			accommodationService.createAccommodation(request.validateToAccommodationInfo(), hostId);

		return ApiResponse.ok(accommodationId);
	}

	@PutMapping("{accommodationId}")
	@Operation(summary = "숙소 수정", description = "숙박 업체가 숙소를 수정합니다.")
	public ApiResponse<Long> updateAccommodation(
		@PathVariable long accommodationId,
		@RequestBody NewAccommodationRequest request,
		@LoginUserId long hostId
	) {
		long updatedAccommodationId =
			accommodationService.updateAccommodation(request.validateToAccommodationInfo(), accommodationId, hostId);

		return ApiResponse.ok(updatedAccommodationId);
	}

	@GetMapping
	@Operation(summary = "숙소 조회", description = "숙박 업체가 숙소를 조회합니다.")
	public ApiResponse<Accommodation> findHostAccommodation(@LoginUserId long hostId) {
		Accommodation findAccommodation = accommodationService.findHostAccommodation(hostId);

		return ApiResponse.ok(findAccommodation);
	}
}

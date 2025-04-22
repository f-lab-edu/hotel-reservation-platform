package com.reservation.host.accommodation.availability.controller;

import static com.reservation.common.response.ApiResponse.*;
import static com.reservation.host.auth.controller.AuthController.*;

import java.util.List;

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
import com.reservation.commonmodel.accommodation.RoomAvailabilityDto;
import com.reservation.host.accommodation.availability.controller.dto.request.CreateRoomAvailabilityRequest;
import com.reservation.host.accommodation.availability.controller.dto.request.RoomAvailabilitySearchCondition;
import com.reservation.host.accommodation.availability.controller.dto.request.UpdateRoomAvailabilityRequest;
import com.reservation.host.accommodation.availability.service.RoomAvailabilityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room-availability")
@Tag(name = "숙소 관리 API", description = "호스트용 숙소 관리 API입니다.")
@PreAuthorize(PRE_AUTH_ROLE_HOST)
@RequiredArgsConstructor
public class RoomAvailabilityController {
	private final RoomAvailabilityService availabilityService;

	@PostMapping
	@Operation(summary = "룸 가용 정보 생성", description = "호스트가 룸 가용 정보를 등록합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Long> createRoomAvailability(@Valid @RequestBody CreateRoomAvailabilityRequest request,
		@Schema(hidden = true) @LoginUserId Long hostId) {
		Long RoomAvailabilityId = availabilityService.createRoomAvailability(request, hostId);
		return ok(RoomAvailabilityId);
	}

	@PutMapping
	@Operation(summary = "룸 가용 정보 수정", description = "호스트가 룸 가용 정보를 수정합니다.")
	public ApiResponse<Long> updateRoomAvailability(@Valid @RequestBody UpdateRoomAvailabilityRequest request,
		@Schema(hidden = true) @LoginUserId Long hostId) {
		Long RoomAvailabilityId = availabilityService.updateRoomAvailability(request, hostId);
		return ok(RoomAvailabilityId);
	}

	@GetMapping
	@Operation(summary = "룸 가용 정보 조회", description = "호스트가 룸 가용 조회합니다.")
	public ApiResponse<List<RoomAvailabilityDto>> findRoomAvailability(
		@Valid @RequestBody RoomAvailabilitySearchCondition condition,
		@Schema(hidden = true) @LoginUserId Long hostId) {
		List<RoomAvailabilityDto> RoomAvailabilityId = availabilityService.findRoomAvailability(condition, hostId);
		return ok(RoomAvailabilityId);
	}
}

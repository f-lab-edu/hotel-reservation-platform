package com.reservation.host.roomavailability.controller;

import static com.reservation.host.auth.controller.AuthController.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.auth.annotation.LoginUserId;
import com.reservation.domain.roomavailability.OriginRoomAvailability;
import com.reservation.host.roomavailability.controller.request.NewRoomAvailabilityRequest;
import com.reservation.host.roomavailability.service.RoomAvailabilityService;
import com.reservation.host.roomavailability.service.dto.DefaultRoomAvailabilityInfo;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room-type")
@Tag(name = "룸 예약 가용 정보 API", description = "룸 예약 가용 정보 관리 API 입니다.")
@PreAuthorize(PRE_AUTH_ROLE_HOST) //✅숙박 업체만 접근 가능
@RequiredArgsConstructor
public class RoomAvailabilityController {
	private final RoomAvailabilityService availabilityService;

	@PostMapping("{roomTypeId}/availability")
	@Operation(summary = "룸 가용 정보 생성", description = "숙박 업체가 룸 가용 정보를 등록합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Long> createRoomAvailability(
		@PathVariable long roomTypeId,
		@Valid @RequestBody NewRoomAvailabilityRequest request,
		@LoginUserId long hostId
	) {
		DefaultRoomAvailabilityInfo createRoomAvailabilityInfo = request.validateToDefaultRoomAvailabilityInfo(
			roomTypeId);

		Long RoomAvailabilityId = availabilityService.createRoomAvailability(createRoomAvailabilityInfo, hostId);

		return ApiResponse.ok(RoomAvailabilityId);
	}

	@PatchMapping("{roomTypeId}/availability/{roomAvailabilityId}")
	@Operation(summary = "룸 가용 정보 수정", description = "숙박 업체가 룸 가용 정보를 수정합니다.")
	public ApiResponse<Long> updateRoomAvailability(
		@PathVariable long roomTypeId,
		@PathVariable long roomAvailabilityId,
		@Valid @RequestBody NewRoomAvailabilityRequest request,
		@LoginUserId long hostId
	) {
		DefaultRoomAvailabilityInfo updateRoomAvailabilityInfo = request.validateToDefaultRoomAvailabilityInfo(
			roomTypeId);

		Long RoomAvailabilityId =
			availabilityService.updateRoomAvailability(updateRoomAvailabilityInfo, roomAvailabilityId, hostId);

		return ApiResponse.ok(RoomAvailabilityId);
	}

	@GetMapping("{roomTypeId}/availability")
	@Operation(summary = "룸 가용 정보 조회", description = "숙박 업체가 룸 가용 조회합니다.")
	public ApiResponse<List<OriginRoomAvailability>> findRoomAvailability(
		@PathVariable long roomTypeId,
		@RequestParam LocalDate startDate,
		@RequestParam LocalDate endDate,
		@LoginUserId long hostId
	) {
		List<OriginRoomAvailability> roomAvailabilities =
			availabilityService.findRoomAvailability(roomTypeId, hostId, startDate, endDate);

		return ApiResponse.ok(roomAvailabilities);
	}
}

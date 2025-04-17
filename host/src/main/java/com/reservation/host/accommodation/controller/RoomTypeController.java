package com.reservation.host.accommodation.controller;

import static com.reservation.host.auth.controller.AuthController.*;

import org.springframework.data.domain.Page;
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

import com.reservation.common.response.ApiResponse;
import com.reservation.commonapi.host.repository.dto.HostRoomTypeDto;
import com.reservation.host.accommodation.controller.dto.request.CreateRoomTypeRequest;
import com.reservation.host.accommodation.controller.dto.request.RoomTypeSearchCondition;
import com.reservation.host.accommodation.controller.dto.request.UpdateRoomTypeRequest;
import com.reservation.host.accommodation.controller.dto.response.FindOneRoomTypeResponse;
import com.reservation.host.accommodation.service.RoomTypeService;
import com.reservation.host.auth.annotation.LoginHost;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room-types")
@Tag(name = "숙소 관리 API", description = "호스트용 숙소 관리 API입니다.")
@PreAuthorize(PRE_AUTH_ROLE_CUSTOMER)
@RequiredArgsConstructor
public class RoomTypeController {
	private final RoomTypeService roomTypeService;

	@PostMapping
	@Operation(summary = "룸 타입 등록", description = "호스트가 룸 타입을 등록합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Long> createRoomType(@Valid @RequestBody CreateRoomTypeRequest request,
		@Schema(hidden = true) @LoginHost Long hostId) {
		Long roomTypeId = roomTypeService.createRoomType(request, hostId);
		return ApiResponse.ok(roomTypeId);
	}

	@PutMapping
	@Operation(summary = "룸 타입 수정", description = "호스트가 룸 타입을 수정합니다.")
	public ApiResponse<Long> updateRoomType(@Valid @RequestBody UpdateRoomTypeRequest request,
		@Schema(hidden = true) @LoginHost Long hostId) {
		Long roomTypeId = roomTypeService.updateRoomType(request, hostId);
		return ApiResponse.ok(roomTypeId);
	}

	@PostMapping("search")
	@Operation(summary = "룸 타입 조회", description = "호스트가 룸 타입을 조회합니다.")
	public ApiResponse<Page<HostRoomTypeDto>> findRoomTypes(@Schema(hidden = true) @LoginHost Long hostId,
		RoomTypeSearchCondition condition) {
		Page<HostRoomTypeDto> roomTypes = roomTypeService.findRoomTypes(hostId, condition);
		return ApiResponse.ok(roomTypes);
	}

	@GetMapping("{id}")
	@Operation(summary = "룸 타입 단일 조회", description = "호스트가 룸 타입을 단일 조회합니다.")
	public ApiResponse<FindOneRoomTypeResponse> findOneRoomType(@Schema(hidden = true) @LoginHost Long hostId,
		@PathVariable Long id) {
		FindOneRoomTypeResponse roomType = roomTypeService.findOneRoomType(hostId, id);
		return ApiResponse.ok(roomType);
	}
}

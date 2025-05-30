package com.reservation.host.roomtype.controller;

import static com.reservation.host.auth.controller.AuthController.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.auth.annotation.LoginUserId;
import com.reservation.domain.roomtype.RoomType;
import com.reservation.host.roomtype.controller.request.NewRoomTypeRequest;
import com.reservation.host.roomtype.controller.request.RoomTypeSearchCondition;
import com.reservation.host.roomtype.service.RoomTypeService;
import com.reservation.host.roomtype.service.dto.SearchRoomTypeResult;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room-type")
@PreAuthorize(PRE_AUTH_ROLE_HOST) //✅숙박 업체만 접근 가능
@RequiredArgsConstructor
@Tag(name = "룸 정보 관리 API", description = "숙박 업체 룸 정보 관리 API 입니다.")
public class RoomTypeController {
	private final RoomTypeService roomTypeService;

	@PostMapping
	@Operation(summary = "룸 타입 등록", description = "숙박 업체가 룸 타입을 등록합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Long> create(
		@RequestBody NewRoomTypeRequest request,
		@LoginUserId long hostId
	) {
		long createdRoomId = roomTypeService.create(request.validateToDefaultRoomInfo(), hostId);

		return ApiResponse.ok(createdRoomId);
	}

	@PatchMapping("{roomTypeId}")
	@Operation(summary = "룸 타입 수정", description = "숙박 업체가 룸 타입을 수정합니다.")
	public ApiResponse<Long> update(
		@PathVariable long roomTypeId,
		@Valid @RequestBody NewRoomTypeRequest request,
		@LoginUserId long hostId
	) {
		long updatedRoomId = roomTypeService.update(roomTypeId, request.validateToDefaultRoomInfo(), hostId);

		return ApiResponse.ok(updatedRoomId);
	}

	@PostMapping("search")
	@Operation(summary = "룸 정보 검색", description = "숙박 업체가 룸 정보를 검색 합니다.")
	public ApiResponse<Page<SearchRoomTypeResult>> search(
		@RequestBody RoomTypeSearchCondition condition,
		@LoginUserId long hostId
	) {
		String roomNameOrNull = condition.nameOrNull();
		PageRequest pageRequest = condition.toPageRequest();

		Page<SearchRoomTypeResult> searchRoomPages = roomTypeService.search(hostId, roomNameOrNull, pageRequest);

		return ApiResponse.ok(searchRoomPages);
	}

	@GetMapping("{roomTypeId}")
	@Operation(summary = "룸 정보 단일 조회", description = "숙박 업체가 룸 정보를 단일 조회합니다.")
	public ApiResponse<RoomType> findOne(
		@PathVariable long roomTypeId,
		@LoginUserId long hostId
	) {
		RoomType findRoomType = roomTypeService.findOne(roomTypeId, hostId);

		return ApiResponse.ok(findRoomType);
	}
}

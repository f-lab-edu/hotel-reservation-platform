package com.reservation.host.room.controller;

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
import com.reservation.domain.room.Room;
import com.reservation.host.room.controller.request.NewRoomRequest;
import com.reservation.host.room.controller.request.RoomSearchCondition;
import com.reservation.host.room.service.RoomService;
import com.reservation.host.room.service.dto.SearchRoomResult;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room")
@PreAuthorize(PRE_AUTH_ROLE_HOST) //✅숙박 업체만 접근 가능
@RequiredArgsConstructor
@Tag(name = "룸 정보 관리 API", description = "숙박 업체 룸 정보 관리 API 입니다.")
public class RoomController {
	private final RoomService roomService;

	@PostMapping
	@Operation(summary = "룸 타입 등록", description = "숙박 업체가 룸 타입을 등록합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Long> create(
		@RequestBody NewRoomRequest request,
		@LoginUserId long hostId
	) {
		long createdRoomId = roomService.create(request.validateToDefaultRoomInfo(), hostId);

		return ApiResponse.ok(createdRoomId);
	}

	@PatchMapping("{roomId}")
	@Operation(summary = "룸 타입 수정", description = "숙박 업체가 룸 타입을 수정합니다.")
	public ApiResponse<Long> update(
		@PathVariable long roomId,
		@Valid @RequestBody NewRoomRequest request,
		@LoginUserId long hostId
	) {
		long updatedRoomId = roomService.update(roomId, request.validateToDefaultRoomInfo(), hostId);

		return ApiResponse.ok(updatedRoomId);
	}

	@PostMapping("search")
	@Operation(summary = "룸 정보 검색", description = "숙박 업체가 룸 정보를 검색 합니다.")
	public ApiResponse<Page<SearchRoomResult>> search(
		@RequestBody RoomSearchCondition condition,
		@LoginUserId long hostId
	) {
		String roomNameOrNull = condition.nameOrNull();
		PageRequest pageRequest = condition.toPageRequest();

		Page<SearchRoomResult> searchRoomPages = roomService.search(hostId, roomNameOrNull, pageRequest);

		return ApiResponse.ok(searchRoomPages);
	}

	@GetMapping("{roomId}")
	@Operation(summary = "룸 정보 단일 조회", description = "숙박 업체가 룸 정보를 단일 조회합니다.")
	public ApiResponse<Room> findOne(
		@PathVariable long roomId,
		@LoginUserId long hostId
	) {
		Room findRoom = roomService.findOne(roomId, hostId);
		
		return ApiResponse.ok(findRoom);
	}
}

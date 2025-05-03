package com.reservation.host.roomimage.controller;

import static com.reservation.host.auth.controller.AuthController.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.auth.annotation.LoginUserId;
import com.reservation.domain.roomimage.RoomImage;
import com.reservation.host.roomimage.controller.request.UpdateRoomImagesRequest;
import com.reservation.host.roomimage.service.RoomImageService;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room-type")
@Tag(name = "룸 이미지 관리 API", description = "숙박 업체 룸 이미지 관리 API 입니다.")
@PreAuthorize(PRE_AUTH_ROLE_HOST) //✅숙박 업체만 접근 가능
@RequiredArgsConstructor
public class RoomImageController {
	private final RoomImageService roomImageService;

	@PutMapping("{roomTypeId}/images")
	@Operation(summary = "룸 이미지(복수) 업데이트", description = "숙박 업체가 룸 이미지(복수)를 업데이트합니다.")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> updateRoomImagesRequest(
		@PathVariable long roomTypeId,
		@RequestBody UpdateRoomImagesRequest request,
		@LoginUserId long hostId) {

		roomImageService.updateRoomImagesRequest(roomTypeId, request.validateRoomImages(), hostId);

		return ApiResponse.noContent();
	}

	@GetMapping("{roomTypeId}/images")
	@Operation(summary = "룸 이미지 URL 전체 조회", description = "숙박 업체가 룸 이미지를 조회합니다.")
	public ApiResponse<List<RoomImage>> findRoomImages(
		@PathVariable long roomTypeId,
		@LoginUserId long hostId) {

		List<RoomImage> roomImages = roomImageService.findRoomImages(roomTypeId, hostId);

		return ApiResponse.ok(roomImages);
	}
}

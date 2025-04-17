package com.reservation.host.accommodation.controller;

import static com.reservation.host.auth.controller.AuthController.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.common.response.ApiResponse;
import com.reservation.commonmodel.accommodation.RoomImageDto;
import com.reservation.host.accommodation.controller.dto.request.UpdateRoomImagesRequest;
import com.reservation.host.accommodation.service.RoomImageService;
import com.reservation.host.auth.annotation.LoginHost;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room-images")
@Tag(name = "숙소 관리 API", description = "호스트용 숙소 관리 API입니다.")
@PreAuthorize(PRE_AUTH_ROLE_CUSTOMER)
@RequiredArgsConstructor
public class RoomImageController {
	private final RoomImageService roomImageService;

	@PutMapping
	@Operation(summary = "룸 이미지(복수) 업데이트", description = "호스트가 룸 이미지(복수)를 업데이트합니다.")
	public ApiResponse<Void> updateRoomImagesRequest(@Valid @RequestBody UpdateRoomImagesRequest request,
		@Schema(hidden = true) @LoginHost Long hostId) {
		roomImageService.updateRoomImagesRequest(request, hostId);
		return ApiResponse.noContent();
	}

	@GetMapping("{roomTypeId}")
	@Operation(summary = "룸 이미지 조회", description = "호스트가 룸 이미지를 조회합니다.")
	public ApiResponse<List<RoomImageDto>> readRoomImagesRequest(@PathVariable Long roomTypeId,
		@Schema(hidden = true) @LoginHost Long hostId) {
		List<RoomImageDto> roomImages = roomImageService.readRoomImagesRequest(roomTypeId, hostId);
		return ApiResponse.ok(roomImages);
	}
}

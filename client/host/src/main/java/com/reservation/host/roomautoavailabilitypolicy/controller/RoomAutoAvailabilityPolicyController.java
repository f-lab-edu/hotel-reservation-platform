package com.reservation.host.roomautoavailabilitypolicy.controller;

import static com.reservation.host.auth.controller.AuthController.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.auth.annotation.LoginUserId;
import com.reservation.host.roomautoavailabilitypolicy.controller.request.NewRoomAutoAvailabilityPolicyRequest;
import com.reservation.host.roomautoavailabilitypolicy.service.RoomAutoAvailabilityPolicyService;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room")
@PreAuthorize(PRE_AUTH_ROLE_HOST) //✅숙박 업체만 접근 가능
@RequiredArgsConstructor
@Tag(name = "룸 예약 가용 자동화 정책 관리 API", description = "숙박 업체 예약 가용 자동화 정책 관리 API 입니다.")
public class RoomAutoAvailabilityPolicyController {
	private final RoomAutoAvailabilityPolicyService roomAutoAvailabilityPolicyService;

	@PostMapping("{roomId}")
	@Operation(summary = "룸 예약 가용 자동화 정책 등록", description = "숙박 업체가 룸 예약 가용 자동화 정책을 등록합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Long> create(
		@PathVariable Long roomId,
		@RequestBody NewRoomAutoAvailabilityPolicyRequest request,
		@LoginUserId Long hostId
	) {
		// long roomTypeId = roomService.create(request.validateToDefaultRoomInfo(), hostId);

		return ApiResponse.ok(1L);
	}
}

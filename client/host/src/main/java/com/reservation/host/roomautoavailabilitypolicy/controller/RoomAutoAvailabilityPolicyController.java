package com.reservation.host.roomautoavailabilitypolicy.controller;

import static com.reservation.host.auth.controller.AuthController.*;

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
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.host.roomautoavailabilitypolicy.controller.request.NewRoomAutoAvailabilityPolicyRequest;
import com.reservation.host.roomautoavailabilitypolicy.service.RoomAutoAvailabilityPolicyService;
import com.reservation.host.roomautoavailabilitypolicy.service.dto.DefaultRoomAutoAvailabilityPolicyInfo;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room-type")
@PreAuthorize(PRE_AUTH_ROLE_HOST) //✅숙박 업체만 접근 가능
@RequiredArgsConstructor
@Tag(name = "룸 예약 가용 자동화 정책 관리 API", description = "숙박 업체 예약 가용 자동화 정책 관리 API 입니다.")
public class RoomAutoAvailabilityPolicyController {
	private final RoomAutoAvailabilityPolicyService roomAutoAvailabilityPolicyService;

	@PostMapping("{roomTypeId}/auto-availability-policy")
	@Operation(summary = "룸 예약 가용 자동화 정책 등록", description = "숙박 업체가 룸 예약 가용 자동화 정책을 등록합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Long> create(
		@PathVariable long roomTypeId,
		@RequestBody NewRoomAutoAvailabilityPolicyRequest request,
		@LoginUserId long hostId
	) {
		DefaultRoomAutoAvailabilityPolicyInfo createRoomAutoAvailabilityPolicyInfo = request.validateToDefaultRoomAutoAvailabilityPolicyInfo();

		long createdRoomAutoAvailabilityPolicyId =
			roomAutoAvailabilityPolicyService.create(roomTypeId, createRoomAutoAvailabilityPolicyInfo, hostId);

		return ApiResponse.ok(createdRoomAutoAvailabilityPolicyId);
	}

	@PatchMapping("{roomTypeId}/auto-availability-policy/{roomAutoAvailabilityPolicyId}")
	@Operation(summary = "룸 예약 가용 자동화 정책 수정", description = "숙박 업체가 룸 예약 가용 자동화 정책을 수정합니다.")
	public ApiResponse<Long> update(
		@PathVariable long roomTypeId,
		@PathVariable long roomAutoAvailabilityPolicyId,
		@RequestBody NewRoomAutoAvailabilityPolicyRequest request,
		@LoginUserId long hostId
	) {
		DefaultRoomAutoAvailabilityPolicyInfo updateRoomAutoAvailabilityPolicyInfo = request.validateToDefaultRoomAutoAvailabilityPolicyInfo();

		long updatedRoomAutoAvailabilityPolicyId =
			roomAutoAvailabilityPolicyService.update(
				roomTypeId,
				roomAutoAvailabilityPolicyId,
				updateRoomAutoAvailabilityPolicyInfo,
				hostId);

		return ApiResponse.ok(updatedRoomAutoAvailabilityPolicyId);
	}

	@GetMapping("{roomTypeId}/auto-availability-policy/{roomAutoAvailabilityPolicyId}")
	@Operation(summary = "룸 예약 가용 자동화 정책 단일 조회", description = "숙박 업체가 룸 예약 가용 자동화 정책를 단일 조회합니다.")
	public ApiResponse<RoomAutoAvailabilityPolicy> findOne(
		@PathVariable long roomTypeId,
		@PathVariable long roomAutoAvailabilityPolicyId,
		@LoginUserId long hostId
	) {
		RoomAutoAvailabilityPolicy findRoomAutoAvailabilityPolicy =
			roomAutoAvailabilityPolicyService.findOne(roomTypeId, roomAutoAvailabilityPolicyId, hostId);

		return ApiResponse.ok(findRoomAutoAvailabilityPolicy);
	}
}

package com.reservation.host.roompricingpolicy.controller;

import static com.reservation.host.auth.controller.AuthController.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.auth.annotation.LoginUserId;
import com.reservation.domain.roompricingpolicy.RoomPricingPolicy;
import com.reservation.host.roompricingpolicy.controller.request.NewRoomPricingPolicyRequest;
import com.reservation.host.roompricingpolicy.service.RoomPricingPolicyService;
import com.reservation.host.roompricingpolicy.service.dto.DefaultRoomPricingPolicyInfo;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room-type")
@PreAuthorize(PRE_AUTH_ROLE_HOST) //✅숙박 업체만 접근 가능
@RequiredArgsConstructor
@Tag(name = "룸 가격 정책 관리 API", description = "숙박 업체 룸 가격 정책 관리 API 입니다.")
public class RoomPricingPolicyController {
	private final RoomPricingPolicyService roomPricingPolicyService;

	@PostMapping("{roomTypeId}/pricing-policy")
	@Operation(summary = "룸 가격 정책 등록", description = "숙박 업체가 가격 정책을 등록합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Long> create(
		@PathVariable long roomTypeId,
		@RequestBody NewRoomPricingPolicyRequest request,
		@LoginUserId long hostId
	) {
		DefaultRoomPricingPolicyInfo createRoomPricingPolicyInfo = request.validateToDefaultRoomPricingPolicyInfo();

		long createdRoomPricingPolicyId =
			roomPricingPolicyService.create(roomTypeId, createRoomPricingPolicyInfo, hostId);

		return ApiResponse.ok(createdRoomPricingPolicyId);
	}

	@PatchMapping("{roomTypeId}/pricing-policy/{roomPricingPolicyId}")
	@Operation(summary = "룸 예약 가용 자동화 정책 수정", description = "숙박 업체가 룸 예약 가용 자동화 정책을 수정합니다.")
	public ApiResponse<Long> update(
		@PathVariable long roomTypeId,
		@PathVariable long roomPricingPolicyId,
		@RequestBody NewRoomPricingPolicyRequest request,
		@LoginUserId long hostId
	) {
		DefaultRoomPricingPolicyInfo updateRoomPricingPolicyInfo = request.validateToDefaultRoomPricingPolicyInfo();

		long updatedRoomAutoAvailabilityPolicyId =
			roomPricingPolicyService.update(
				roomTypeId,
				roomPricingPolicyId,
				updateRoomPricingPolicyInfo,
				hostId);

		return ApiResponse.ok(updatedRoomAutoAvailabilityPolicyId);
	}

	@DeleteMapping("{roomTypeId}/pricing-policy/{roomPricingPolicyId}")
	@Operation(summary = "룸 예약 가용 자동화 정책 수정", description = "숙박 업체가 룸 예약 가용 자동화 정책을 수정합니다.")
	public ApiResponse<Long> delete(
		@PathVariable long roomTypeId,
		@PathVariable long roomPricingPolicyId,
		@LoginUserId long hostId
	) {
		roomPricingPolicyService.delete(roomTypeId, roomPricingPolicyId, hostId);

		return ApiResponse.noContent();
	}

	@GetMapping("{roomTypeId}/pricing-policy/{roomPricingPolicyId}")
	@Operation(summary = "룸 예약 가용 자동화 정책 단일 조회", description = "숙박 업체가 룸 예약 가용 자동화 정책를 단일 조회합니다.")
	public ApiResponse<RoomPricingPolicy> findOne(
		@PathVariable long roomTypeId,
		@PathVariable long roomPricingPolicyId,
		@LoginUserId long hostId
	) {
		RoomPricingPolicy findRoomPricingPolicy = roomPricingPolicyService.findOne(roomTypeId, roomPricingPolicyId,
			hostId);

		return ApiResponse.ok(findRoomPricingPolicy);
	}
}

package com.reservation.customer.reservation.controller;

import static com.reservation.customer.auth.controller.AuthController.*;
import static com.reservation.support.response.ApiResponse.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.auth.annotation.LoginUserId;
import com.reservation.customer.reservation.controller.request.RoomReservationRequest;
import com.reservation.customer.reservation.service.ReservationService;
import com.reservation.customer.reservation.service.dto.CreateReservationResult;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reservations")
@Tag(name = "객실 예약 API", description = "객실 예약 관리 API 입니다.")
@PreAuthorize(PRE_AUTH_ROLE_CUSTOMER) //✅일반 고객만 접근 가능
@RequiredArgsConstructor
public class ReservationController {
	private final ReservationService reservationService;

	@PostMapping()
	@Operation(summary = "객실 예약 생성 API", description = "결제 직전 상태의 임시 예약 정보를 생성합니다.")
	public ApiResponse<CreateReservationResult> createRoomReservation(
		@LoginUserId Long memberId,
		@RequestBody RoomReservationRequest request
	) {
		CreateReservationResult result =
			reservationService.createReservation(memberId, request.validateToCreateCommand());
		return ok(result);
	}

}

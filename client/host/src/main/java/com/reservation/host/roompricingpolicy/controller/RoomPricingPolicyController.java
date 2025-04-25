package com.reservation.host.roompricingpolicy.controller;

import static com.reservation.host.auth.controller.AuthController.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.host.roompricingpolicy.service.RoomPricingPolicyService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/room")
@PreAuthorize(PRE_AUTH_ROLE_HOST) //✅숙박 업체만 접근 가능
@RequiredArgsConstructor
@Tag(name = "룸 가격 정책 관리 API", description = "숙박 업체 룸 가격 정책 관리 API 입니다.")
public class RoomPricingPolicyController {
	private final RoomPricingPolicyService roomPricingPolicyService;
}

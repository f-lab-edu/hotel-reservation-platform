package com.reservation.customer.payment.controller;

import static com.reservation.support.response.ApiResponse.*;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.customer.payment.controller.request.PaymentCheckRequest;
import com.reservation.customer.payment.service.PaymentService;
import com.reservation.customer.payment.service.dto.IamportPayment;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payment")
@Tag(name = "객실 예약 API", description = "객실 예약 관리 API 입니다.")
@RequiredArgsConstructor
public class PaymentController {
	private final PaymentService paymentService;

	@PostMapping()
	@Operation(summary = "결제 확인 API (결제 취소 시 낙관적 락 활용)", description = "결제가 정상적으로 처리 됐는지 확인합니다.")
	@CrossOrigin(origins = "http://localhost:63342", allowCredentials = "true")
	public ApiResponse<IamportPayment> optimisticValidationPayment(@RequestBody @Valid PaymentCheckRequest request) {
		IamportPayment result =
			paymentService.optimisticValidationPayment(request.paymentUid(), request.reservationId());

		return ok(result);
	}

	@PostMapping("redisson")
	@Operation(summary = "결제 확인 API (결제 취소 시 레디슨 락 활용)", description = "결제가 정상적으로 처리 됐는지 확인합니다.")
	@CrossOrigin(origins = "http://localhost:63342", allowCredentials = "true")
	public ApiResponse<IamportPayment> redissonValidationPayment(@RequestBody @Valid PaymentCheckRequest request) {
		IamportPayment result =
			paymentService.redissonValidationPayment(request.paymentUid(), request.reservationId());

		return ok(result);
	}
}

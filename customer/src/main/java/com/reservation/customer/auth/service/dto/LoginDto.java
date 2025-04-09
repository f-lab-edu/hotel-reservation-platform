package com.reservation.customer.auth.service.dto;

public record LoginDto(
	Long memberId,
	String accessToken,
	String refreshToken
) {
}

package com.reservation.host.auth.service.dto;

public record LoginDto(
	Long memberId,
	String accessToken,
	String refreshToken
) {
}

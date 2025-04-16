package com.reservation.commonmodel.host;

public record HostDto(
	Long id,
	String email,
	HostStatus status,
	String password) {
}

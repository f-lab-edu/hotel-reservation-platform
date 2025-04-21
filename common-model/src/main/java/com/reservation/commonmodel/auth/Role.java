package com.reservation.commonmodel.auth;

import java.util.Arrays;

public enum Role {
	HOST("ROLE_HOST"),
	CUSTOMER("ROLE_CUSTOMER"),
	ADMIN("ROLE_ADMIN");

	private final String authority;

	Role(String authority) {
		this.authority = authority;
	}

	public String authority() {
		return authority;
	}

	public static Role fromAuthority(String authority) {
		return Arrays.stream(values())
			.filter(r -> r.authority.equals(authority))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unknown role: " + authority));
	}
}

package com.reservation.commonmodel.host;

public enum HostStatus {
	ACTIVE("정상 계정"),
	SUSPENDED("정지 계정"),
	WITHDRAWN("탈퇴 계정");

	private final String label;

	HostStatus(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}

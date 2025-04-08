package com.reservation.commonmodel.member;

public enum MemberStatus {
	ACTIVE("정상 계정"),
	INACTIVE("휴면 계정"),
	SUSPENDED("정지 계정"),
	WITHDRAWN("탈퇴 계정");

	private final String label;

	MemberStatus(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}

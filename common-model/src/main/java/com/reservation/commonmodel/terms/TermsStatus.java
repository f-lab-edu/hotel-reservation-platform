package com.reservation.commonmodel.terms;

public enum TermsStatus {
	ACTIVE("현재 사용 중"),
	INACTIVE("더 이상 노출하지 않음"),
	DEPRECATED("이전 버전, 변경됨");

	private final String label;

	TermsStatus(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}

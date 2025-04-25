package com.reservation.domain.terms.enums;

public enum TermsType {
	REQUIRED("필수"),
	OPTIONAL("선택");

	private final String label;

	TermsType(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}

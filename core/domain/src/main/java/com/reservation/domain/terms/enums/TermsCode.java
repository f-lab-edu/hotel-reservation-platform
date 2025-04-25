package com.reservation.domain.terms.enums;

public enum TermsCode {
	TERMS_USE("이용약관"),
	TERMS_PRIVACY("개인정보처리방침"),
	TERMS_MARKETING("마케팅정보 수집 및 이용 동의"),
	TERMS_LOCATION("위치 정보 수집 및 이용 동의"),
	;

	private final String label;

	TermsCode(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}

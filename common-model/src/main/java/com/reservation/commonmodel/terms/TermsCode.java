package com.reservation.commonmodel.terms;

public enum TermsCode {
	TERMS_USE("이용약관"),
	TERMS_PRIVACY("개인정보처리방침");

	private final String label;

	TermsCode(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}

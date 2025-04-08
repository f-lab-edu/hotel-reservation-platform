package com.reservation.commonmodel.member;

import java.time.LocalDateTime;

import com.reservation.commonmodel.terms.TermsDto;

public record MemberTermsDto(
	Boolean isAgreed,
	LocalDateTime agreedAt,
	MemberDto memberDto,
	TermsDto termsDto
) {
}

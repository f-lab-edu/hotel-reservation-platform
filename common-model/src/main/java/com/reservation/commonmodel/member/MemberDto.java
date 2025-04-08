package com.reservation.commonmodel.member;

import java.time.LocalDateTime;
import java.util.List;

public record MemberDto(
	Long id,
	String password,
	MemberStatus status,
	String email,
	String phoneNumber,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	List<MemberTermsDto> memberTerms
) {
}

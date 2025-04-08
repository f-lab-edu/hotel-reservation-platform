package com.reservation.customer.member.service.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.reservation.commonmodel.member.MemberDto;
import com.reservation.commonmodel.member.MemberStatus;
import com.reservation.commonmodel.member.MemberTermsDto;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.customer.member.controller.dto.request.SignupRequest;

public class MemberDtoMapper {
	public static MemberDto fromSignupRequestAndPasswordAndTermsDtoList(SignupRequest request, String password,
		List<TermsDto> termsDtoList) {
		LocalDateTime now = LocalDateTime.now();
		return new MemberDto(
			null,
			password,
			MemberStatus.ACTIVE,
			request.email(),
			request.phoneNumber().replaceAll("-", ""),
			null,
			null,
			termsDtoList.stream().map(termsDto ->
				new MemberTermsDto(true, now, null, termsDto)).toList()
		);
	}
}

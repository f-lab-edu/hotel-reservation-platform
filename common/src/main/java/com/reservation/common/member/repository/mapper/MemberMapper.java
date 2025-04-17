package com.reservation.common.member.repository.mapper;

import java.util.List;

import com.reservation.common.member.domain.Member;
import com.reservation.common.member.domain.Member.MemberBuilder;
import com.reservation.common.member.domain.MemberTerms;
import com.reservation.common.member.domain.MemberTerms.MemberTermsBuilder;
import com.reservation.common.terms.repository.mapper.TermsMapper;
import com.reservation.commonmodel.member.MemberDto;
import com.reservation.commonmodel.member.MemberTermsDto;

public class MemberMapper {
	public static MemberDto fromEntityToDto(Member member) {
		return new MemberDto(
			member.getId(),
			member.getPassword(),
			member.getStatus(),
			member.getEmail(),
			member.getPhoneNumber(),
			member.getCreatedAt(),
			member.getUpdatedAt(),
			member.getMemberTermsList()
				.stream()
				.map((memberTerms) ->
					new MemberTermsDto(memberTerms.getIsAgreed(), memberTerms.getAgreedAt(), null, null))
				.toList()
		);
	}

	public static Member fromDtoToEntity(MemberDto memberDto) {
		Member member = new MemberBuilder()
			.email(memberDto.email())
			.phoneNumber(memberDto.phoneNumber())
			.password(memberDto.password())
			.status(memberDto.status())
			.build();

		List<MemberTerms> memberTermsList = memberDto.memberTerms()
			.stream()
			.map((memberTermsDto) ->
				new MemberTermsBuilder()
					.terms(TermsMapper.fromDtoToEntity(memberTermsDto.termsDto()))
					.member(member)
					.agreedAt(memberTermsDto.agreedAt())
					.isAgreed(memberTermsDto.isAgreed())
					.build())
			.toList();

		member.addMemberTermsList(memberTermsList);

		return member;
	}
}

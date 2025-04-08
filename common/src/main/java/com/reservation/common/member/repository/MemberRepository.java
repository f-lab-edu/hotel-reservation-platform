package com.reservation.common.member.repository;

import static com.reservation.common.member.repository.mapper.MemberDtoMapper.*;

import org.springframework.stereotype.Repository;

import com.reservation.commonapi.customer.repository.CustomerMemberRepository;
import com.reservation.commonmodel.member.MemberDto;
import com.reservation.commonmodel.member.MemberStatus;

@Repository
public class MemberRepository implements CustomerMemberRepository {
	private final JpaMemberRepository jpaMemberRepository;

	public MemberRepository(JpaMemberRepository jpaMemberRepository) {
		this.jpaMemberRepository = jpaMemberRepository;
	}

	@Override
	public Boolean existsByPhoneNumberAndStatus(String phoneNumber, MemberStatus status) {
		return jpaMemberRepository.existsByPhoneNumberAndStatus(phoneNumber, status);
	}

	@Override
	public Boolean existsByEmailAndStatus(String email, MemberStatus status) {
		return jpaMemberRepository.existsByEmailAndStatus(email, status);
	}

	@Override
	public MemberDto save(MemberDto memberDto) {
		return fromMember(jpaMemberRepository.save(toMember(memberDto)));
	}
}

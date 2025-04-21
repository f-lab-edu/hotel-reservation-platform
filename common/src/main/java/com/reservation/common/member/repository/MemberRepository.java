package com.reservation.common.member.repository;

import static com.reservation.common.member.repository.mapper.MemberMapper.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.reservation.common.member.repository.mapper.MemberMapper;
import com.reservation.commonapi.customer.repository.CustomerMemberRepository;
import com.reservation.commonmodel.member.MemberDto;
import com.reservation.commonmodel.member.MemberStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepository implements CustomerMemberRepository {
	private final JpaMemberRepository jpaMemberRepository;

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
		return fromEntityToDto(jpaMemberRepository.save(fromDtoToEntity(memberDto)));
	}

	@Override
	public Optional<MemberDto> findOneByEmailAndStatusIsNot(String email, MemberStatus status) {
		return jpaMemberRepository.findOneByEmailAndStatusIsNot(email, status).map(MemberMapper::fromEntityToDto);
	}

	@Override
	public Optional<MemberDto> findById(Long memberId) {
		return jpaMemberRepository.findById(memberId).map(MemberMapper::fromEntityToDto);
	}
}

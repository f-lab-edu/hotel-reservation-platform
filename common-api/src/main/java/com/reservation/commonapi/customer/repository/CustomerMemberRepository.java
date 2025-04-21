package com.reservation.commonapi.customer.repository;

import java.util.Optional;

import com.reservation.commonmodel.member.MemberDto;
import com.reservation.commonmodel.member.MemberStatus;

public interface CustomerMemberRepository {
	Boolean existsByPhoneNumberAndStatus(String phoneNumber, MemberStatus status); // 휴대폰 번호 중복 체크

	Boolean existsByEmailAndStatus(String email, MemberStatus status); // 이메일 중복 체크

	MemberDto save(MemberDto memberDto); // 고객 정보 저장

	Optional<MemberDto> findOneByEmailAndStatusIsNot(String email, MemberStatus status);

	Optional<MemberDto> findById(Long memberId);
}

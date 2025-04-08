package com.reservation.common.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.common.member.domain.Member;
import com.reservation.commonmodel.member.MemberStatus;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {
	Boolean existsByPhoneNumberAndStatus(String phoneNumber, MemberStatus status);

	Boolean existsByEmailAndStatus(String email, MemberStatus status);
}

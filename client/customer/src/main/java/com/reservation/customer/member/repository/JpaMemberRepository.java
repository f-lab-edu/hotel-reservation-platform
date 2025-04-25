package com.reservation.customer.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.member.Member;
import com.reservation.domain.member.enums.MemberStatus;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {
	Boolean existsByPhoneNumberAndStatus(String phoneNumber, MemberStatus status);

	Boolean existsByEmailAndStatus(String email, MemberStatus status);

	Optional<Member> findOneByEmailAndStatusIsNot(String email, MemberStatus status);
}

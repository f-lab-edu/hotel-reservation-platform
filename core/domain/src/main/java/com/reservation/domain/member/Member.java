package com.reservation.domain.member.domain;

import java.util.ArrayList;
import java.util.List;

import com.reservation.domain.base.BaseEntity;
import com.reservation.websupport.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
public class Member extends BaseEntity {
	@Column(nullable = true)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MemberStatus status;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String phoneNumber;

	@Transient
	private final List<MemberTerms> memberTermsList = new ArrayList<>();

	protected Member() {
	}

	@Builder
	public Member(String password, MemberStatus status, String email, String phoneNumber) {
		if (status == null) {
			throw ErrorCode.CONFLICT.exception("상태는 필수입니다.");
		}
		if (email == null || email.isEmpty()) {
			throw ErrorCode.CONFLICT.exception("이메일은 필수입니다.");
		}
		if (phoneNumber == null || phoneNumber.isEmpty()) {
			throw ErrorCode.CONFLICT.exception("전화번호는 필수입니다.");
		}
		this.password = password;
		this.status = status;
		this.email = email;
		this.phoneNumber = phoneNumber;
	}

	public void addMemberTermsList(List<MemberTerms> memberTermsList) {
		this.memberTermsList.addAll(memberTermsList);
	}
}

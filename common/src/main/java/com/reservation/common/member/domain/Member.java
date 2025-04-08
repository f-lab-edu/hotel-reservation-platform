package com.reservation.common.member.domain;

import java.util.ArrayList;
import java.util.List;

import com.reservation.common.domain.BaseEntity;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.member.MemberStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import lombok.Getter;

@Entity
public class Member extends BaseEntity {
	@Column(nullable = false)
	private String password;

	@Getter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MemberStatus status;

	@Getter
	@Column(nullable = false)
	private String email;

	@Getter
	@Column(nullable = false)
	private String phoneNumber;

	@Getter
	@Transient
	private final List<MemberTerms> memberTermsList = new ArrayList<>();

	protected Member() {
	}

	public Member(String password, MemberStatus status, String email, String phoneNumber) {
		if (password == null || password.isEmpty()) {
			throw ErrorCode.CONFLICT.exception("비빌 번호는 필수입니다.");
		}
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

	public static class MemberBuilder {
		private String password;
		private MemberStatus status;
		private String email;
		private String phoneNumber;

		public MemberBuilder password(String password) {
			this.password = password;
			return this;
		}

		public MemberBuilder status(MemberStatus status) {
			this.status = status;
			return this;
		}

		public MemberBuilder email(String email) {
			this.email = email;
			return this;
		}

		public MemberBuilder phoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
			return this;
		}

		public Member build() {
			return new Member(password, status, email, phoneNumber);
		}
	}

	public void addMemberTermsList(List<MemberTerms> memberTermsList) {
		this.memberTermsList.addAll(memberTermsList);
	}
}

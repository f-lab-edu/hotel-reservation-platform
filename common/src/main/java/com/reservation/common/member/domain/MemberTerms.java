package com.reservation.common.member.domain;

import java.time.LocalDateTime;

import com.reservation.common.domain.BaseEntity;
import com.reservation.common.terms.domain.Terms;
import com.reservation.commonmodel.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Entity
public class MemberTerms extends BaseEntity {
	@Getter
	@Column(nullable = false)
	private Boolean isAgreed;

	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "terms_id", nullable = false)
	private Terms terms;

	@Getter
	@Column(nullable = false)
	private LocalDateTime agreedAt;

	protected MemberTerms() {
	}

	public MemberTerms(Boolean isAgreed, Member member, Terms terms, LocalDateTime agreedAt) {
		if (isAgreed == null) {
			throw ErrorCode.NOT_FOUND.exception("isAgreed는 필수입니다.");
		}
		if (agreedAt == null) {
			throw ErrorCode.NOT_FOUND.exception("동의 일시는 필수입니다.");
		}
		if (terms == null) {
			throw ErrorCode.NOT_FOUND.exception("약관은 필수입니다.");
		}
		if (member == null) {
			throw ErrorCode.NOT_FOUND.exception("회원은 필수입니다.");
		}

		this.isAgreed = isAgreed;
		this.member = member;
		this.terms = terms;
		this.agreedAt = agreedAt;
	}

	public static class MemberTermsBuilder {
		private Boolean isAgreed;
		private Member member;
		private Terms terms;
		private LocalDateTime agreedAt;

		public MemberTermsBuilder isAgreed(Boolean isAgreed) {
			this.isAgreed = isAgreed;
			return this;
		}

		public MemberTermsBuilder member(Member member) {
			this.member = member;
			return this;
		}

		public MemberTermsBuilder terms(Terms terms) {
			this.terms = terms;
			return this;
		}

		public MemberTermsBuilder agreedAt(LocalDateTime agreedAt) {
			this.agreedAt = agreedAt;
			return this;
		}

		public MemberTerms build() {
			return new MemberTerms(isAgreed, member, terms, agreedAt);
		}
	}
}

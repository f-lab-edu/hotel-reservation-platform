package com.reservation.domain.member;

import java.time.LocalDateTime;

import com.reservation.domain.base.BaseEntity;
import com.reservation.domain.terms.Terms;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class MemberTerms extends BaseEntity {
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

	@Builder
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
}

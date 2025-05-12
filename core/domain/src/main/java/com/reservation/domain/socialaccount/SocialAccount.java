package com.reservation.domain.socialaccount;

import com.reservation.domain.base.BaseEntity;
import com.reservation.support.enums.SocialLoginProvider;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class SocialAccount extends BaseEntity {
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SocialLoginProvider provider;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private Long memberId; // Member ID

	@Builder
	public SocialAccount(SocialLoginProvider provider, String email, Long memberId) {
		if (provider == null) {
			throw ErrorCode.UNAUTHORIZED.exception("소셜 로그인 제공자는 필수 값입니다.");
		}
		if (email == null || email.isBlank()) {
			throw ErrorCode.UNAUTHORIZED.exception("이메일은 필수 값입니다.");
		}
		if (memberId == null || memberId <= 0) {
			throw ErrorCode.UNAUTHORIZED.exception("Member ID는 필수 값입니다.");
		}
		this.provider = provider;
		this.email = email;
		this.memberId = memberId;
	}
}

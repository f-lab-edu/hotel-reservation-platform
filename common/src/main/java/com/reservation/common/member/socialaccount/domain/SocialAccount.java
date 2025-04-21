package com.reservation.common.member.socialaccount.domain;

import com.reservation.common.domain.BaseEntity;
import com.reservation.commonmodel.auth.login.SocialLoginProvider;
import com.reservation.commonmodel.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Getter
@Entity
public class SocialAccount extends BaseEntity {
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SocialLoginProvider provider;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	Long memberId; // Member ID

	protected SocialAccount() {
	}

	public SocialAccount(SocialLoginProvider provider, String email, Long memberId) {
		if (provider == null) {
			throw ErrorCode.UNAUTHORIZED.exception("Provider cannot be null");
		}
		if (email == null || email.isBlank()) {
			throw ErrorCode.UNAUTHORIZED.exception("Email cannot be null or blank");
		}
		if (memberId == null || memberId <= 0) {
			throw ErrorCode.UNAUTHORIZED.exception("Member ID must be a positive number");
		}
		this.provider = provider;
		this.email = email;
		this.memberId = memberId;
	}
}

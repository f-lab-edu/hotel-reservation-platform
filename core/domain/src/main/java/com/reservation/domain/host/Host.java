package com.reservation.domain.host;

import com.reservation.domain.base.BaseEntity;
import com.reservation.domain.host.enums.HostStatus;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
public class Host extends BaseEntity {
	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private HostStatus status;

	protected Host() {
	}

	@Builder
	public Host(Long id, String password, String email, HostStatus status) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("아이디는 0보다 커야합니다.");
		}
		if (password == null || password.isBlank()) {
			throw ErrorCode.CONFLICT.exception("비밀번호는 필수입니다.");
		}
		if (email == null || email.isBlank()) {
			throw ErrorCode.CONFLICT.exception("이메일은 필수입니다.");
		}
		if (status == null) {
			throw ErrorCode.CONFLICT.exception("상태는 필수입니다.");
		}

		this.id = id;
		this.password = password;
		this.email = email;
		this.status = status;
	}
}

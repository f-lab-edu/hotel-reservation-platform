package com.reservation.domain.uploadedimage;

import java.time.LocalDate;

import com.reservation.auth.login.Role;
import com.reservation.domain.base.BaseEntity;
import com.reservation.domain.uploadedimage.enums.ImageDomain;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
public class UploadedImage extends BaseEntity {

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ImageDomain domain;

	@Column(nullable = false)
	private LocalDate uploadDate;

	@Column(nullable = false, unique = true)
	private String uuid;

	@Column(nullable = false)
	private Long uploaderId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Role uploaderRole;

	protected UploadedImage() {
	}

	@Builder
	public UploadedImage(
		Long id, ImageDomain domain, LocalDate uploadDate, String uuid, Long uploaderId, Role uploaderRole) {

		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("ID 값은 0보다 커야 합니다.");
		}
		if (domain == null) {
			throw ErrorCode.BAD_REQUEST.exception("도메인 값은 null일 수 없습니다.");
		}
		if (uploadDate == null) {
			throw ErrorCode.BAD_REQUEST.exception("업로드 날짜는 null일 수 없습니다.");
		}
		if (uuid == null || uuid.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("UUID 값은 null이거나 비어있을 수 없습니다.");
		}
		if (uploaderId == null || uploaderId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("업로더 ID 값은 null이거나 0보다 작을 수 없습니다.");
		}
		if (uploaderRole == null) {
			throw ErrorCode.BAD_REQUEST.exception("업로더 역할은 null일 수 없습니다.");
		}

		this.id = id;
		this.domain = domain;
		this.uploadDate = uploadDate;
		this.uuid = uuid;
		this.uploaderId = uploaderId;
		this.uploaderRole = uploaderRole;
	}
}

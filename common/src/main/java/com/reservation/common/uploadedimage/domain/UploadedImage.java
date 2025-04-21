package com.reservation.common.uploadedimage.domain;

import java.time.LocalDate;

import com.reservation.common.domain.BaseEntity;
import com.reservation.commonmodel.auth.Role;
import com.reservation.commonmodel.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	public UploadedImage(Long id, ImageDomain domain, LocalDate uploadDate, String uuid, Long uploaderId,
		Role uploaderRole) {
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

	public static class UploadedImageBuilder {
		private Long id;
		private ImageDomain domain;
		private LocalDate uploadDate;
		private String uuid;
		private Long uploaderId;
		private Role uploaderRole;

		public UploadedImageBuilder id(Long id) {
			this.id = id;
			return this;
		}

		public UploadedImageBuilder domain(ImageDomain domain) {
			this.domain = domain;
			return this;
		}

		public UploadedImageBuilder uploadDate(LocalDate uploadDate) {
			this.uploadDate = uploadDate;
			return this;
		}

		public UploadedImageBuilder uuid(String uuid) {
			this.uuid = uuid;
			return this;
		}

		public UploadedImageBuilder uploaderId(Long uploaderId) {
			this.uploaderId = uploaderId;
			return this;
		}

		public UploadedImageBuilder uploaderRole(Role uploaderRole) {
			this.uploaderRole = uploaderRole;
			return this;
		}

		public UploadedImage build() {
			return new UploadedImage(id, domain, uploadDate, uuid, uploaderId, uploaderRole);
		}
	}
}

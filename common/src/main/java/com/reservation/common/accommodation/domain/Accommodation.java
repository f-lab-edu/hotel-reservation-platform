package com.reservation.common.accommodation.domain;

import com.reservation.common.domain.BaseEntity;
import com.reservation.common.host.domain.Host;
import com.reservation.commonmodel.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;

@Getter
@Entity
public class Accommodation extends BaseEntity {
	@OneToOne()
	@JoinColumn(name = "host_id")
	private Host host; // 숙소 소유자

	@Column(nullable = false)
	private String name; // 숙소 이름

	@Column(nullable = true)
	private String descriptionOrNull; // 숙소 설명은 생략 가능

	@Column(nullable = false)
	private String location; // 숙소 위치

	@Column(nullable = false)
	private Boolean isVisible; // 노출 여부 (기본 false)

	@Column(nullable = true)
	private String mainImageUrlOrNull; // 숙소 대표 이미지 URL 생략 가능

	@Column(nullable = false)
	private String contactNumber; // 연락처

	protected Accommodation() {
	}

	public Accommodation(Long id, Host host, String name, String descriptionOrNull, String location,
		Boolean isVisible, String mainImageUrlOrNull, String contactNumber) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("숙소 ID는 0보다 커야 합니다.");
		}
		if (host == null) {
			throw ErrorCode.CONFLICT.exception("숙소 소유자는 필수 입니다.");
		}
		if (name == null || name.isBlank()) {
			throw ErrorCode.CONFLICT.exception("숙소 이름은 필수 입니다.");
		}
		if (location == null || location.isBlank()) {
			throw ErrorCode.CONFLICT.exception("숙소 위치는 필수 입니다.");
		}
		if (contactNumber == null || contactNumber.isBlank()) {
			throw ErrorCode.CONFLICT.exception("숙소 연락처는 필수 입니다.");
		}
		if (mainImageUrlOrNull != null && !mainImageUrlOrNull.startsWith("http")) {
			throw ErrorCode.CONFLICT.exception("숙소 대표 이미지 URL은 http 또는 https로 시작해야 합니다.");
		}
		if (descriptionOrNull != null && descriptionOrNull.length() > 1000) {
			throw ErrorCode.CONFLICT.exception("숙소 설명은 1000자 이내로 작성해야 합니다.");
		}
		if (isVisible == null) {
			isVisible = false; // 기본값 설정
		}

		this.id = id;
		this.host = host;
		this.name = name;
		this.descriptionOrNull = descriptionOrNull;
		this.location = location;
		this.isVisible = isVisible;
		this.mainImageUrlOrNull = mainImageUrlOrNull;
		this.contactNumber = contactNumber;
	}

	public static class AccommodationBuilder {
		private Long id;
		private Host host;
		private String name;
		private String descriptionOrNull;
		private String location;
		private Boolean isVisible;
		private String mainImageUrlOrNull;
		private String contactNumber;

		public AccommodationBuilder id(Long id) {
			this.id = id;
			return this;
		}

		public AccommodationBuilder host(Host host) {
			this.host = host;
			return this;
		}

		public AccommodationBuilder name(String name) {
			this.name = name;
			return this;
		}

		public AccommodationBuilder descriptionOrNull(String descriptionOrNull) {
			this.descriptionOrNull = descriptionOrNull;
			return this;
		}

		public AccommodationBuilder location(String location) {
			this.location = location;
			return this;
		}

		public AccommodationBuilder isVisible(Boolean isVisible) {
			this.isVisible = isVisible;
			return this;
		}

		public AccommodationBuilder mainImageUrlOrNull(String mainImageUrlOrNull) {
			this.mainImageUrlOrNull = mainImageUrlOrNull;
			return this;
		}

		public AccommodationBuilder contactNumber(String contactNumber) {
			this.contactNumber = contactNumber;
			return this;
		}

		public Accommodation build() {
			return new Accommodation(id, host, name, descriptionOrNull, location, isVisible, mainImageUrlOrNull,
				contactNumber);
		}
	}

}

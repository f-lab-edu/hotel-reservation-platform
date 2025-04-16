package com.reservation.common.accommodation.domain;

import com.reservation.commonmodel.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class Location {

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "latitude", nullable = false)
	private Double latitude;

	@Column(name = "longitude", nullable = false)
	private Double longitude;

	protected Location() {
	}

	public Location(String address, Double latitude, Double longitude) {
		if (address == null || address.isBlank()) {
			throw ErrorCode.CONFLICT.exception("주소는 필수입니다.");
		}
		if (latitude == null || latitude < -90 || latitude > 90) {
			throw ErrorCode.CONFLICT.exception("위도는 -90 ~ 90 사이의 값이어야 합니다.");
		}
		if (longitude == null || longitude < -180 || longitude > 180) {
			throw ErrorCode.CONFLICT.exception("경도는 -180 ~ 180 사이의 값이어야 합니다.");
		}
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}

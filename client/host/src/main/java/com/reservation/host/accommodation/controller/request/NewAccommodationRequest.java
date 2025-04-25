package com.reservation.host.accommodation.controller.request;

import com.reservation.domain.accommodation.Location;
import com.reservation.host.accommodation.service.dto.DefaultAccommodationInfo;
import com.reservation.support.exception.ErrorCode;

public record NewAccommodationRequest(
	String name,
	String descriptionOrNull,
	String address,
	Double latitude,
	Double longitude,
	Boolean isVisible,
	String mainImageUrlOrNull,
	String contactNumber
) {
	public DefaultAccommodationInfo validateToAccommodationInfo() {
		if (name == null || name.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("숙소명은 필수 입니다.");
		}
		if (address == null || address.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("숙소 주소는 필수 입니다.");
		}
		if (latitude == null || latitude < -90 || latitude > 90) {
			throw ErrorCode.BAD_REQUEST.exception("위도는 -90 ~ 90 사이의 값이어야 합니다.");
		}
		if (longitude == null || longitude < -180 || longitude > 180) {
			throw ErrorCode.BAD_REQUEST.exception("경도는 -180 ~ 180 사이의 값이어야 합니다.");
		}
		if (contactNumber == null || contactNumber.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("연락처는 필수 입니다.");
		}
		if (isVisible == null) {
			throw ErrorCode.BAD_REQUEST.exception("노출 여부는 필수 입니다.");
		}
		if (mainImageUrlOrNull != null && mainImageUrlOrNull.isBlank()) {
			throw ErrorCode.BAD_REQUEST.exception("숙소 대표 이미지 URL은 비어있을 수 없습니다.");
		}
		if (descriptionOrNull != null && descriptionOrNull.length() > 1000) {
			throw ErrorCode.BAD_REQUEST.exception("숙소 설명은 1000자 이내여야 합니다.");
		}
		return new DefaultAccommodationInfo(
			name,
			descriptionOrNull,
			new Location(address, latitude, longitude),
			isVisible,
			mainImageUrlOrNull,
			contactNumber
		);
	}
}

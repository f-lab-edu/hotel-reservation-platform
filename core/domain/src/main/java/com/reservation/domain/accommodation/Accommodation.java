package com.reservation.domain.accommodation;

import com.reservation.domain.base.BaseEntity;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.SqlResultSetMapping;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@SqlResultSetMapping(
	name = "RoomAvailabilitySearchResultMapping",
	classes = @ConstructorResult(
		targetClass = RoomAvailabilitySearchResult.class,
		columns = {
			@ColumnResult(name = "accommodation_id", type = Long.class),
			@ColumnResult(name = "room_type_id", type = Long.class),
			@ColumnResult(name = "accommodation_name", type = String.class),
			@ColumnResult(name = "total_price", type = Integer.class),
		}
	)
)
public class Accommodation extends BaseEntity {
	@Column(nullable = false)
	private Long hostId; // 숙소 소유자

	@Column(nullable = false)
	private String name; // 숙소 이름

	@Column(nullable = true, name = "description")
	private String descriptionOrNull; // 숙소 설명은 생략 가능

	@Embedded
	private Location location; // 숙소 위치

	@Column(nullable = false)
	private Boolean isVisible; // 노출 여부 (기본 false)

	@Column(nullable = true, name = "main_image_url")
	private String mainImageUrlOrNull; // 숙소 대표 이미지 URL 생략 가능

	@Column(nullable = false)
	private String contactNumber; // 연락처

	@Builder
	public Accommodation(
		Long id,
		Long hostId,
		String name,
		String descriptionOrNull,
		Location location,
		Boolean isVisible,
		String mainImageUrlOrNull,
		String contactNumber) {
		if (id != null && id <= 0) {
			throw ErrorCode.CONFLICT.exception("숙소 ID는 0보다 커야 합니다.");
		}
		if (hostId == null || hostId <= 0) {
			throw ErrorCode.CONFLICT.exception("숙소 소유자는 필수 입니다.");
		}
		if (name == null || name.isBlank()) {
			throw ErrorCode.CONFLICT.exception("숙소 이름은 필수 입니다.");
		}
		if (location == null) {
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
		this.hostId = hostId;
		this.name = name;
		this.descriptionOrNull = descriptionOrNull;
		this.location = location;
		this.isVisible = isVisible;
		this.mainImageUrlOrNull = mainImageUrlOrNull;
		this.contactNumber = contactNumber;
	}
}

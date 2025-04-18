package com.reservation.commonmodel.accommodation;

public record RoomImageDto(
	Long id,
	Long roomTypeId,
	String imageUrl,
	Integer displayOrder,
	Boolean isMainImage
) {
}

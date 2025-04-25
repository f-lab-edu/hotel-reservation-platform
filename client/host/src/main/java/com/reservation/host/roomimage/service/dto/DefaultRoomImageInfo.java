package com.reservation.host.roomimage.service.dto;

public record DefaultRoomImageInfo(
	Long id,
	String uploadUrl,
	int displayOrder,
	boolean isMainImage
) {
}

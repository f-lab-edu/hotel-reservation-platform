package com.reservation.customer.roomavailability.repository.dto;

public record AvailableRoomTypeResult(
	Long roomTypeId,
	String name,
	Integer capacity,
	Integer totalPrice, // 전체 숙박일수 기준 총 가격
	String thumbnailImageUrl, // 객실 썸네일 이미지
	Integer remainingCount
) {
}

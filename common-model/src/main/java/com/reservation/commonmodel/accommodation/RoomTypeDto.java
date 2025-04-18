package com.reservation.commonmodel.accommodation;

public record RoomTypeDto(
	Long id,
	Long accommodationId,
	String name,
	Integer capacity,
	Integer price,
	String descriptionOrNull,
	Integer roomCount
) {
}

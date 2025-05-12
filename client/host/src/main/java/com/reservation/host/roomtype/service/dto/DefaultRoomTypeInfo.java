package com.reservation.host.roomtype.service.dto;

public record DefaultRoomTypeInfo(
	long accommodationId,
	String name,
	int capacity,
	int price,
	String descriptionOrNull,
	int roomCount
) {
}

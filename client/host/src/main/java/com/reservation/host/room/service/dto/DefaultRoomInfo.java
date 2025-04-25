package com.reservation.host.room.service.dto;

public record DefaultRoomInfo(
	long accommodationId,
	String name,
	int capacity,
	int price,
	String descriptionOrNull,
	int roomCount
) {
}

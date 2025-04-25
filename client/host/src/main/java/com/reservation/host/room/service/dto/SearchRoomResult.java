package com.reservation.host.room.service.dto;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class SearchRoomResult {
	long id;
	long accommodationId;
	String name;
	int capacity;
	int price;
	String descriptionOrNull;
	int roomCount;
	String mainImageUrl;

	@QueryProjection
	public SearchRoomResult(
		long id,
		long accommodationId,
		String name,
		int capacity,
		int price,
		String descriptionOrNull,
		int roomCount,
		String mainImageUrl
	) {
		this.id = id;
		this.accommodationId = accommodationId;
		this.name = name;
		this.capacity = capacity;
		this.price = price;
		this.descriptionOrNull = descriptionOrNull;
		this.roomCount = roomCount;
		this.mainImageUrl = mainImageUrl;
	}
}

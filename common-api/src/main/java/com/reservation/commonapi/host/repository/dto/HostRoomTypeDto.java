package com.reservation.commonapi.host.repository.dto;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class HostRoomTypeDto {
	Long id;
	Long accommodationId;
	String name;
	Integer capacity;
	Integer price;
	String descriptionOrNull;
	Integer roomCount;
	String mainImageUrl;

	@QueryProjection
	public HostRoomTypeDto(
		Long id,
		Long accommodationId,
		String name,
		Integer capacity,
		Integer price,
		String descriptionOrNull,
		Integer roomCount,
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

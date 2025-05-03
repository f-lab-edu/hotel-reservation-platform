package com.reservation.host.roomtype.controller.request;

import com.reservation.querysupport.sort.SortField;

import lombok.Getter;

@Getter
public enum RoomTypeSortField implements SortField {
	NAME("name"), // 룸 이름
	PRICE("price"), // 룸 가격
	CAPACITY("capacity"); // 룸 수용 인원

	private final String fieldName;

	RoomTypeSortField(String fieldName) {
		this.fieldName = fieldName;
	}
}


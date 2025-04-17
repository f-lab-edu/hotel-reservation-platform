package com.reservation.commonapi.host.query.sort;

import com.reservation.commonmodel.sort.SortField;

import lombok.Getter;

@Getter
public enum HostRoomTypeSortField implements SortField {
	NAME("name"), // 룸 이름
	PRICE("price"), // 룸 가격
	CAPACITY("capacity"); // 룸 수용 인원
	
	private final String fieldName;

	HostRoomTypeSortField(String fieldName) {
		this.fieldName = fieldName;
	}
}


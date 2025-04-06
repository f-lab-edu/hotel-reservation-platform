package com.reservation.commonapi.customer.query.sort;

import com.reservation.commonmodel.sort.SortField;

import lombok.Getter;

@Getter
public enum CustomerTermsSortField implements SortField {
	CREATED_AT("createdAt"),// 생성일
	DISPLAY_ORDER("displayOrder"); // 노출순서

	private final String fieldName;

	CustomerTermsSortField(String fieldName) {
		this.fieldName = fieldName;
	}
}

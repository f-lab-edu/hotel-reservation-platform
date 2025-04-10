package com.reservation.commonapi.admin.query.sort;

import com.reservation.commonmodel.sort.SortField;

import lombok.Getter;

@Getter
public enum AdminTermsSortField implements SortField {
	EXPOSED_FROM("exposedFrom"), // 노출 시작일
	EXPOSED_TO("exposedToOrNull"), // 노출 종료일
	CREATED_AT("createdAt"), // 생성일
	TITLE("title"), // 제목
	ID("id"); // ID

	private final String fieldName;

	AdminTermsSortField(String fieldName) {
		this.fieldName = fieldName;
	}
}

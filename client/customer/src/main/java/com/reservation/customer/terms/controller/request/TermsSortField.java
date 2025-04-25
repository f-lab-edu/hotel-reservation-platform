package com.reservation.customer.terms.controller.request;

import com.reservation.querysupport.sort.SortField;

import lombok.Getter;

@Getter
public enum TermsSortField implements SortField {
	CREATED_AT("createdAt"), // 생성일
	TITLE("title"); // 제목

	private final String fieldName;

	TermsSortField(String fieldName) {
		this.fieldName = fieldName;
	}
}

package com.reservation.admin.terms.service.mapper;

import com.reservation.admin.terms.controller.dto.request.CreateClauseRequest;
import com.reservation.admin.terms.controller.dto.request.UpdateClauseRequest;
import com.reservation.commonmodel.terms.ClauseDto;

public class ClauseDtoMapper {
	public static ClauseDto fromCreateClauseRequest(CreateClauseRequest request) {
		return new ClauseDto(
			null, // id
			request.clauseOrder(),
			request.title(),
			request.content()
		);
	}

	public static ClauseDto fromUpdateClauseRequest(UpdateClauseRequest request) {
		return new ClauseDto(
			null,
			request.clauseOrder(),
			request.title(),
			request.content()
		);
	}
}

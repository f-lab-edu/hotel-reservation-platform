package com.reservation.admin.terms.service.mapper;

import com.reservation.admin.terms.controller.dto.CreateClauseRequest;
import com.reservation.admin.terms.controller.dto.UpdateClauseRequest;
import com.reservation.commonapi.terms.repository.dto.AdminClauseDto;

public class AdminClauseDtoMapper {
	public static AdminClauseDto fromCreateClauseRequest(CreateClauseRequest request) {
		return new AdminClauseDto(
			null, // id
			request.clauseOrder(),
			request.title(),
			request.content()
		);
	}

	public static AdminClauseDto fromUpdateClauseRequest(UpdateClauseRequest request) {
		return new AdminClauseDto(
			null,
			request.clauseOrder(),
			request.title(),
			request.content()
		);
	}
}

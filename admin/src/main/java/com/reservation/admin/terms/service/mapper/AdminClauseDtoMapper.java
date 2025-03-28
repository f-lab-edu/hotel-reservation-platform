package com.reservation.admin.terms.service.mapper;

import com.reservation.admin.terms.controller.dto.AdminCreateClauseRequest;
import com.reservation.commonapi.terms.repository.dto.AdminClauseDto;

public class AdminClauseDtoMapper {
	public static AdminClauseDto fromAdminCreateClauseRequest(AdminCreateClauseRequest request) {
		return new AdminClauseDto(
			null, // id
			request.clauseOrder(),
			request.title(),
			request.content()
		);
	}
}

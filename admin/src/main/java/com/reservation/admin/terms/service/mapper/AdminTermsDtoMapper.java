package com.reservation.admin.terms.service.mapper;

import com.reservation.admin.terms.controller.dto.AdminCreateTermsRequest;
import com.reservation.commonapi.terms.repository.dto.AdminTermsDto;

public class AdminTermsDtoMapper {

	public static AdminTermsDto fromAdminCreateTermsRequest(AdminCreateTermsRequest request) {
		return new AdminTermsDto(
			null, // id
			request.code(),
			request.title(),
			request.type(),
			request.status(),
			null, // rowVersion
			request.exposedFrom(),
			request.exposedTo(),
			request.displayOrder(),
			null, // createdAt
			null, // updatedAt
			request.clauses().stream()
				.map(AdminClauseDtoMapper::fromAdminCreateClauseRequest)
				.toList()
		);
	}
}

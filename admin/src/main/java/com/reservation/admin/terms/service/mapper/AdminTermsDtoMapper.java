package com.reservation.admin.terms.service.mapper;

import com.reservation.admin.terms.controller.dto.CreateTermsRequest;
import com.reservation.admin.terms.controller.dto.UpdateTermsRequest;
import com.reservation.commonapi.terms.repository.dto.AdminTermsDto;

public class AdminTermsDtoMapper {

	public static AdminTermsDto fromAdminCreateTermsRequestAndVersion(CreateTermsRequest request,
		Integer version) {
		return new AdminTermsDto(
			null, // id
			request.code(),
			request.title(),
			request.type(),
			request.status(),
			version,
			request.exposedFrom(),
			request.exposedTo(),
			request.displayOrder(),
			null, // createdAt
			null, // updatedAt
			request.clauses().stream()
				.map(AdminClauseDtoMapper::fromCreateClauseRequest)
				.toList()
		);
	}

	public static AdminTermsDto fromAdminUpdateTermsRequestAndVersion(UpdateTermsRequest request,
		Integer version) {
		return new AdminTermsDto(
			request.id(), // id
			request.code(),
			request.title(),
			request.type(),
			request.status(),
			version,
			request.exposedFrom(),
			request.exposedTo(),
			request.displayOrder(),
			null, // createdAt
			null, // updatedAt
			request.clauses().stream()
				.map(AdminClauseDtoMapper::fromUpdateClauseRequest)
				.toList()
		);
	}
}

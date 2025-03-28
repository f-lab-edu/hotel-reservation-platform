package com.reservation.admin.terms.service.mapper;

import com.reservation.admin.terms.controller.dto.CreateTermsRequest;
import com.reservation.admin.terms.controller.dto.UpdateTermsRequest;
import com.reservation.commonapi.terms.repository.dto.TermsDto;

public class TermsDtoMapper {

	public static TermsDto fromCreateTermsRequestAndVersion(CreateTermsRequest request,
		Integer version) {
		return new TermsDto(
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
				.map(ClauseDtoMapper::fromCreateClauseRequest)
				.toList()
		);
	}

	public static TermsDto fromUpdateTermsRequestAndVersion(UpdateTermsRequest request,
		Integer version) {
		return new TermsDto(
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
				.map(ClauseDtoMapper::fromUpdateClauseRequest)
				.toList()
		);
	}
}

package com.reservation.admin.terms.service.mapper;

import com.reservation.admin.terms.controller.dto.request.CreateTermsRequest;
import com.reservation.admin.terms.controller.dto.request.UpdateTermsRequest;
import com.reservation.commonmodel.terms.TermsDto;

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
			true, // isLatest
			request.exposedFrom(),
			request.exposedToOrNull(),
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
			null, // id
			request.code(),
			request.title(),
			request.type(),
			request.status(),
			version,
			true, // isLatest
			request.exposedFrom(),
			request.exposedToOrNull(),
			request.displayOrder(),
			null, // createdAt
			null, // updatedAt
			request.clauses().stream()
				.map(ClauseDtoMapper::fromUpdateClauseRequest)
				.toList()
		);
	}
}

package com.reservation.customer.terms.service.mapper;

import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.customer.terms.controller.dto.response.TermsDetailResponse;

public class TermsDetailResponseMapper {
	public static TermsDetailResponse fromTermsDtoToResponse(TermsDto terms) {
		return new TermsDetailResponse(
			terms.id(),
			terms.code(),
			terms.title(),
			terms.type(),
			terms.status(),
			terms.version(),
			terms.exposedFrom(),
			terms.displayOrder(),
			terms.clauses()
		);
	}
}

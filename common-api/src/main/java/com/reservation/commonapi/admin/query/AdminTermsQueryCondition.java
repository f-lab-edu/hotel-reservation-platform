package com.reservation.commonapi.admin.query;

import org.springframework.data.domain.PageRequest;

import com.reservation.commonmodel.terms.TermsCode;

public record AdminTermsQueryCondition(
	TermsCode code,
	boolean includeAllVersions,
	PageRequest pageRequest
) {
}

package com.reservation.admin.terms.service.mapper;

import com.reservation.admin.terms.controller.dto.response.TermsSearchCondition;
import com.reservation.commonapi.terms.query.condition.AdminTermsQueryCondition;

public class AdminTermsQueryConditionMapper {
	public static AdminTermsQueryCondition fromSearchConditionToQueryCondition(TermsSearchCondition condition) {
		return new AdminTermsQueryCondition(
			condition.code(),
			Boolean.TRUE.equals(condition.includeAllVersions()),
			condition.toPageRequest()
		);
	}
}

package com.reservation.admin.terms.service.mapper;

import com.reservation.admin.terms.controller.dto.request.TermsSearchCondition;
import com.reservation.commonapi.admin.query.AdminTermsQueryCondition;

public class AdminTermsQueryConditionMapper {
	public static AdminTermsQueryCondition fromSearchConditionToQueryCondition(TermsSearchCondition condition) {
		return new AdminTermsQueryCondition(
			condition.code(),
			Boolean.TRUE.equals(condition.includeAllVersions()),
			condition.toPageRequest()
		);
	}
}

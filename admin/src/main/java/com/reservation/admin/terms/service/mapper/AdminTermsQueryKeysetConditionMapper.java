package com.reservation.admin.terms.service.mapper;

import com.reservation.admin.terms.controller.dto.request.TermsKeysetSearchCondition;
import com.reservation.commonapi.admin.query.AdminTermsKeysetQueryCondition;

public class AdminTermsQueryKeysetConditionMapper {
	public static AdminTermsKeysetQueryCondition fromSearchConditionToQueryKeysetCondition(
		TermsKeysetSearchCondition condition) {
		return new AdminTermsKeysetQueryCondition(
			condition.code(),
			Boolean.TRUE.equals(condition.includeAllVersions()),
			condition.size(),
			condition.cursors()
		);
	}
}

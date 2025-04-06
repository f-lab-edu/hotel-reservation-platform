package com.reservation.customer.terms.service.mapper;

import com.reservation.commonapi.customer.query.CustomerTermsQueryCondition;
import com.reservation.customer.terms.controller.dto.request.TermsSearchCondition;

public class CustomerTermsQueryConditionMapper {
	public static CustomerTermsQueryCondition fromSearchConditionToQueryCondition(TermsSearchCondition condition) {
		return new CustomerTermsQueryCondition(
			condition.toPageRequest()
		);
	}
}

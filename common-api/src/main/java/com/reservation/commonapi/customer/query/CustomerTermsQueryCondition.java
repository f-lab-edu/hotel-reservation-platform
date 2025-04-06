package com.reservation.commonapi.customer.query;

import org.springframework.data.domain.PageRequest;

public record CustomerTermsQueryCondition(
	PageRequest pageRequest
) {
}

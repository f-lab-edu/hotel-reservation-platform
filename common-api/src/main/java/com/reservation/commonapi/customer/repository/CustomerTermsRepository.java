package com.reservation.commonapi.customer.repository;

import org.springframework.data.domain.Page;

import com.reservation.commonapi.customer.query.CustomerTermsQueryCondition;
import com.reservation.commonapi.customer.repository.dto.CustomerTermsDto;

public interface CustomerTermsRepository {
	Page<CustomerTermsDto> findTermsByCondition(CustomerTermsQueryCondition condition); // Query Condition 조회

}

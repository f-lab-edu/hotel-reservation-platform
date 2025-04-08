package com.reservation.commonapi.customer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.reservation.commonapi.customer.query.CustomerTermsQueryCondition;
import com.reservation.commonapi.customer.repository.dto.CustomerTermsDto;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;

public interface CustomerTermsRepository {
	Page<CustomerTermsDto> findTermsByCondition(CustomerTermsQueryCondition condition); // Query Condition 조회

	Optional<TermsDto> findById(Long id); // 약관 ID로 단일 조회

	Optional<TermsDto> findWithClausesById(Long id); // 약관 ID로 단일 조회

	List<TermsDto> findRequiredTerms();

	List<TermsDto> findByStatusAndIsLatest(TermsStatus status, Boolean isLatest);
}

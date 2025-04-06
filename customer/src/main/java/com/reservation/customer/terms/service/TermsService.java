package com.reservation.customer.terms.service;

import static com.reservation.customer.terms.service.mapper.CustomerTermsQueryConditionMapper.*;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.reservation.commonapi.customer.query.CustomerTermsQueryCondition;
import com.reservation.commonapi.customer.repository.CustomerTermsRepository;
import com.reservation.commonapi.customer.repository.dto.CustomerTermsDto;
import com.reservation.customer.terms.controller.dto.request.TermsSearchCondition;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class TermsService {
	private final CustomerTermsRepository termsRepository;

	public Page<CustomerTermsDto> findTerms(TermsSearchCondition condition) {
		CustomerTermsQueryCondition queryCondition = fromSearchConditionToQueryCondition(condition);

		return this.termsRepository.findTermsByCondition(queryCondition);
	}
}

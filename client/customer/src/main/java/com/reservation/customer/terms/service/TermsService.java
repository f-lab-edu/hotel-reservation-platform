package com.reservation.customer.terms.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.reservation.customer.terms.controller.request.TermsSearchCondition;
import com.reservation.customer.terms.repository.TermsQueryRepository;
import com.reservation.customer.terms.service.dto.SearchTerms;
import com.reservation.domain.terms.Terms;
import com.reservation.domain.terms.enums.TermsStatus;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class TermsService {
	private final TermsQueryRepository termsQueryRepository;

	public Page<SearchTerms> findTerms(TermsSearchCondition condition) {
		PageRequest pageRequest = condition.toPageRequest();

		return termsQueryRepository.findTermsByCondition(pageRequest);
	}

	public Terms findById(Long id) {
		Terms findTerms = termsQueryRepository.findWithClausesById(id)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("존재하지 않는 약관입니다."));

		if (!findTerms.getIsLatest()) {
			throw ErrorCode.BAD_REQUEST.exception("공개된 약관이 아닙니다.");
		}

		if (findTerms.getStatus() != TermsStatus.ACTIVE) {
			throw ErrorCode.BAD_REQUEST.exception("현재 사용 중인 약관이 아닙니다.");
		}

		return findTerms;
	}
}

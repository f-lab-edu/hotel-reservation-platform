package com.reservation.customer.terms.service;

import static com.reservation.customer.terms.service.mapper.TermsDetailResponseMapper.*;
import static com.reservation.customer.terms.service.mapper.TermsQueryConditionMapper.*;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.reservation.commonapi.customer.query.CustomerTermsQueryCondition;
import com.reservation.commonapi.customer.repository.CustomerTermsRepository;
import com.reservation.commonapi.customer.repository.dto.CustomerTermsDto;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.customer.terms.controller.dto.request.TermsSearchCondition;
import com.reservation.customer.terms.controller.dto.response.TermsDetailResponse;

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

	public TermsDetailResponse findById(Long id) {
		TermsDto termsDto = this.termsRepository.findWithClausesById(id)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("존재하지 않는 약관입니다."));

		if (!termsDto.isLatest()) {
			throw ErrorCode.BAD_REQUEST.exception("공개된 약관이 아닙니다.");
		}
		if (termsDto.status() != TermsStatus.ACTIVE) {
			throw ErrorCode.BAD_REQUEST.exception("현재 사용 중인 약관이 아닙니다.");
		}

		return fromTermsDtoToResponse(termsDto);
	}
}

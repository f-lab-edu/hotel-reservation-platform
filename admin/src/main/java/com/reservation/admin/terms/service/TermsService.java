package com.reservation.admin.terms.service;

import static com.reservation.admin.terms.service.mapper.AdminTermsDtoMapper.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.reservation.admin.terms.controller.dto.AdminCreateTermsRequest;
import com.reservation.common.exception.ErrorCode;
import com.reservation.commonapi.terms.repository.AdminTermsRepository;
import com.reservation.commonapi.terms.repository.dto.AdminTermsDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class TermsService {
	private final AdminTermsRepository adminTermsRepository;

	public Long createTerms(AdminCreateTermsRequest request) {
		checkActiveTermsExists(request.code());

		// Versioning
		int nextVersion = this.adminTermsRepository.findMaxVersionByCode(request.code()).orElse(0) + 1;
		AdminTermsDto adminTermsDto = fromAdminCreateTermsRequestAndVersion(request, nextVersion);

		try {
			return adminTermsRepository.save(adminTermsDto).id();
		} catch (DataIntegrityViolationException dataIntegrityViolationException) {
			throw ErrorCode.CONFLICT.exception("동일한 약관이 이미 등록되었습니다.");
		}
	}

	public void checkActiveTermsExists(TermsCode code) {
		boolean existsActiveTerms = adminTermsRepository.existsByCodeAndStatus(code, TermsStatus.ACTIVE);
		if (existsActiveTerms) {
			throw ErrorCode.BAD_REQUEST.exception("이미 사용 중인 약관이 존재합니다. 기존 약관을 수정하세요.");
		}
	}
}

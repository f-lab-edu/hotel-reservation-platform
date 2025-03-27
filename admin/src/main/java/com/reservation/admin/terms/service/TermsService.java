package com.reservation.admin.terms.service;

import static com.reservation.admin.terms.service.mapper.AdminTermsDtoMapper.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.reservation.admin.terms.controller.dto.AdminCreateTermsRequest;
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
	private static final int NOTHING_VERSION = 0;

	private final AdminTermsRepository adminTermsRepository;

	public Long createTerms(AdminCreateTermsRequest request) {
		checkActiveTermsExists(request.code());

		// Versioning
		int maxVersion = this.adminTermsRepository.findMaxVersionByCode(request.code()).orElse(NOTHING_VERSION);
		AdminTermsDto adminTermsDto = fromAdminCreateTermsRequestAndVersion(request, ++maxVersion);

		try {
			return adminTermsRepository.save(adminTermsDto).id();
		} catch (DataIntegrityViolationException dataIntegrityViolationException) {
			throw new IllegalArgumentException("동일한 약관이 이미 등록되었습니다.");
		}
	}

	public void checkActiveTermsExists(TermsCode code) {
		boolean existsActiveTerms = adminTermsRepository.existsByCodeAndStatus(code, TermsStatus.ACTIVE);
		if (existsActiveTerms) {
			throw new IllegalArgumentException("이미 사용 중인 약관이 존재합니다. 기존 약관을 수정하세요.");
		}
	}
}

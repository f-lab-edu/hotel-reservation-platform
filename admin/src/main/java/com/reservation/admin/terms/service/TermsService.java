package com.reservation.admin.terms.service;

import static com.reservation.admin.terms.service.mapper.AdminTermsDtoMapper.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.reservation.admin.terms.controller.dto.AdminCreateTermsRequest;
import com.reservation.admin.terms.controller.dto.AdminUpdateTermsRequest;
import com.reservation.common.exception.ErrorCode;
import com.reservation.common.support.retry.OptimisticLockingFailureRetryUtils;
import com.reservation.common.terms.service.TermsCommandService;
import com.reservation.commonapi.terms.repository.AdminTermsRepository;
import com.reservation.commonapi.terms.repository.dto.AdminTermsDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class TermsService {
	private static final int NOTHING_VERSION = 0;
	private static final int MAX_TERMS_SAVE_OPTIMISTIC_LOCK_RETRY_COUNT = 3;

	private final AdminTermsRepository adminTermsRepository;
	private final TermsCommandService termsCommandService;

	public Long createTerms(AdminCreateTermsRequest request) {
		checkActiveTermsExists(request.code());

		// Versioning
		int maxVersion = this.adminTermsRepository.findMaxVersionByCode(request.code()).orElse(NOTHING_VERSION);
		AdminTermsDto adminTermsDto = fromAdminCreateTermsRequestAndVersion(request, ++maxVersion);

		try {
			return this.adminTermsRepository.save(adminTermsDto).id();
		} catch (DataIntegrityViolationException dataIntegrityViolationException) {
			throw ErrorCode.CONFLICT.exception("데이터 무결성 위반으로 인한 작업 실패, 데이터 확인 요청 필요");
		}
	}

	public void checkActiveTermsExists(TermsCode code) {
		boolean existsActiveTerms = adminTermsRepository.existsByCodeAndStatus(code, TermsStatus.ACTIVE);
		if (existsActiveTerms) {
			throw ErrorCode.BAD_REQUEST.exception("이미 사용 중인 약관이 존재합니다. 기존 약관을 수정하세요.");
		}
	}

	@Transactional
	public Long updateTerms(AdminUpdateTermsRequest request) {
		AdminTermsDto findAdminTermsDto = this.adminTermsRepository.findById(request.id())
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("약관이 존재하지 않습니다."));

		this.termsCommandService.deprecateWithoutIncrement(findAdminTermsDto.code());

		AdminTermsDto updateAdminTermsDto = fromAdminUpdateTermsRequestAndVersion(request,
			findAdminTermsDto.rowVersion() + 1);

		return OptimisticLockingFailureRetryUtils.executeWithRetry(MAX_TERMS_SAVE_OPTIMISTIC_LOCK_RETRY_COUNT,
			() -> this.adminTermsRepository.save(updateAdminTermsDto).id());
	}
}

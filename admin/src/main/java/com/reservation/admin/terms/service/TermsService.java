package com.reservation.admin.terms.service;

import static com.reservation.admin.terms.service.mapper.TermsDtoMapper.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.reservation.admin.terms.controller.dto.CreateTermsRequest;
import com.reservation.admin.terms.controller.dto.UpdateTermsRequest;
import com.reservation.common.exception.ErrorCode;
import com.reservation.common.terms.service.TermsCommandService;
import com.reservation.commonapi.terms.repository.AdminTermsRepository;
import com.reservation.commonapi.terms.repository.dto.TermsDto;
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

	private final AdminTermsRepository adminTermsRepository;
	private final TermsCommandService termsCommandService;

	public Long createTerms(CreateTermsRequest request) {
		checkActiveTermsExists(request.code());

		// Versioning
		int maxVersion = this.adminTermsRepository.findMaxVersionByCode(request.code()).orElse(NOTHING_VERSION);
		TermsDto createdTerms = fromCreateTermsRequestAndVersion(request, ++maxVersion);

		return saveTermsWithIntegrityCheck(createdTerms);
	}

	public void checkActiveTermsExists(TermsCode code) {
		boolean existsActiveTerms = adminTermsRepository.existsByCodeAndStatus(code, TermsStatus.ACTIVE);
		if (existsActiveTerms) {
			throw ErrorCode.BAD_REQUEST.exception("이미 사용 중인 약관이 존재합니다. 기존 약관을 수정하세요.");
		}
	}

	@Transactional
	public Long updateTerms(UpdateTermsRequest request) {
		// 가장 최신 약관 버전이 맞는지 확인한다
		int maxVersion = checkUpdateTermsVersion(request.id());

		// 기존 약관을 deprecate 한다
		termsCommandService.deprecateTerms(request.id());

		// Versioning
		TermsDto updatedTerms = fromUpdateTermsRequestAndVersion(request, ++maxVersion);

		return saveTermsWithIntegrityCheck(updatedTerms);
	}

	public int checkUpdateTermsVersion(Long id) {
		TermsDto findTermsDto = this.adminTermsRepository.findById(id)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("약관이 존재하지 않습니다."));

		// 같은 약관 코드 중 가창 최신 약관 버전을 가져온다
		int maxVersion = this.adminTermsRepository.findMaxVersionByCode(findTermsDto.code())
			.orElseThrow(() -> ErrorCode.CONFLICT.exception("올바른 약관 버전을 찾을 수 없습니다."));

		if (findTermsDto.version() != maxVersion) {
			throw ErrorCode.BAD_REQUEST.exception("과거 버전의 약관은 수정할 수 없습니다.");
		}

		return maxVersion;
	}

	public Long saveTermsWithIntegrityCheck(TermsDto termsDto) {
		try {
			return this.adminTermsRepository.save(termsDto).id();
		} catch (DataIntegrityViolationException dataIntegrityViolationException) {
			throw ErrorCode.CONFLICT.exception("데이터 무결성 위반으로 인한 작업 실패, 데이터 확인 요청 필요");
		}
	}
}

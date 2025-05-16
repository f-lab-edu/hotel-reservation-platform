package com.reservation.admin.terms.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.reservation.admin.terms.controller.request.TermsCursor;
import com.reservation.admin.terms.repository.JpaTermsRepository;
import com.reservation.admin.terms.repository.TermsQueryRepository;
import com.reservation.admin.terms.repository.dto.SearchTermsResult;
import com.reservation.domain.terms.Terms;
import com.reservation.domain.terms.enums.TermsCode;
import com.reservation.domain.terms.enums.TermsStatus;
import com.reservation.querysupport.page.KeysetPage;
import com.reservation.support.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class TermsService {
	private static final int NOTHING_VERSION = 0;

	private final JpaTermsRepository jpaTermsRepository;
	private final TermsQueryRepository termsQueryRepository;

	@Transactional
	public long create(Terms requestCreateTerms) {
		checkActiveTermsExists(requestCreateTerms.getCode());

		// 약관 Versioning
		int maxVersion = jpaTermsRepository.findMaxVersionByCode(requestCreateTerms.getCode()).orElse(NOTHING_VERSION);

		requestCreateTerms.setNewVersionAndIdInitialization(++maxVersion);

		return saveTermsWithIntegrityCheck(requestCreateTerms);
	}

	private void checkActiveTermsExists(TermsCode code) {
		boolean existsActiveTerms = jpaTermsRepository.existsByCodeAndStatus(code, TermsStatus.ACTIVE);
		if (existsActiveTerms) {
			throw ErrorCode.BAD_REQUEST.exception("이미 사용 중인 약관이 존재합니다. 기존 약관을 수정하세요.");
		}
	}

	private long saveTermsWithIntegrityCheck(Terms saveTerms) {
		try {
			return jpaTermsRepository.save(saveTerms).getId();
		} catch (DataIntegrityViolationException e) {
			log.warn(e.getMessage());
			throw ErrorCode.CONFLICT.exception("데이터 무결성 위반으로 인한 작업 실패, 데이터 확인 요청 필요");
		}
	}

	@Transactional
	public long update(Terms requestNewVersionTerms) {
		// 가장 최신 약관 버전이 맞는지 확인한다
		int maxVersion = checkUpdateTermsVersion(requestNewVersionTerms.getId());

		// 기존 약관을 deprecate 한다
		requestNewVersionTerms.deprecate();
		jpaTermsRepository.saveAndFlush(requestNewVersionTerms);

		// New Versioning 세팅 및 ID 초기화
		requestNewVersionTerms.setNewVersionAndIdInitialization(++maxVersion);

		return saveTermsWithIntegrityCheck(requestNewVersionTerms);
	}

	private int checkUpdateTermsVersion(long id) {
		Terms findTerms = jpaTermsRepository.findById(id)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("약관이 존재하지 않습니다."));

		// 같은 약관 코드 중 가창 최신 약관 버전을 가져온다
		int maxVersion = jpaTermsRepository.findMaxVersionByCode(findTerms.getCode())
			.orElseThrow(() -> ErrorCode.CONFLICT.exception("올바른 약관 버전을 찾을 수 없습니다."));

		if (findTerms.getVersion() != maxVersion) {
			throw ErrorCode.BAD_REQUEST.exception("과거 버전의 약관은 수정할 수 없습니다.");
		}

		return maxVersion;
	}

	public Terms findById(long id) {
		return termsQueryRepository.findWithClausesById(id)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("존재하지 않는 약관입니다."));
	}

	public Page<SearchTermsResult> searchTermsFormatPage(
		TermsCode searchCodeOrNull,
		boolean isLatest,
		PageRequest pageRequest
	) {
		return termsQueryRepository.searchTermsFormatPage(searchCodeOrNull, isLatest, pageRequest);
	}

	public KeysetPage<SearchTermsResult, TermsCursor> searchTermsFormatCursor(
		TermsCode searchCodeOrNull,
		boolean isLatest,
		int size,
		List<TermsCursor> cursors
	) {
		return termsQueryRepository.findTermsByKeysetCondition(searchCodeOrNull, isLatest, size, cursors);
	}
}

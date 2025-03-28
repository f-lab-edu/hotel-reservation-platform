package com.reservation.commonapi.terms.repository;

import java.util.Optional;

import com.reservation.commonapi.terms.repository.dto.AdminTermsDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;

public interface AdminTermsRepository {
	AdminTermsDto save(AdminTermsDto terms); // 약관 저장

	boolean existsByCodeAndStatus(TermsCode code, TermsStatus status); // 약관 코드와 상태로 존재하는지 확인

	Optional<Integer> findMaxVersionByCode(TermsCode code); // 약관 코드 최신 버전을 찾음

	Optional<AdminTermsDto> findById(Long id); // 약관 ID로 단일 조회

	Optional<AdminTermsDto> findByCodeAndStatus(TermsCode code, TermsStatus termsStatus); // 약관 코드와 상태로 단일 조회
}

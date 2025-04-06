package com.reservation.commonapi.admin.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.reservation.commonapi.admin.query.AdminTermsKeysetQueryCondition;
import com.reservation.commonapi.admin.query.AdminTermsQueryCondition;
import com.reservation.commonapi.admin.query.sort.AdminTermsSortCursor;
import com.reservation.commonapi.admin.repository.dto.AdminTermsDto;
import com.reservation.commonmodel.keyset.KeysetPage;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;

public interface AdminTermsRepository {
	TermsDto save(TermsDto terms); // 약관 저장

	boolean existsByCodeAndStatus(TermsCode code, TermsStatus status); // 약관 코드와 상태로 존재하는지 확인

	Optional<Integer> findMaxVersionByCode(TermsCode code); // 약관 코드 최신 버전을 찾음

	Optional<TermsDto> findById(Long id); // 약관 ID로 단일 조회

	Page<AdminTermsDto> findTermsByCondition(AdminTermsQueryCondition condition); // Query Condition 조회

	KeysetPage<AdminTermsDto, AdminTermsSortCursor> findTermsByKeysetCondition(
		AdminTermsKeysetQueryCondition condition); // Query keyset Condition 조회
}

package com.reservation.commonapi.terms.repository;

import com.reservation.commonapi.terms.repository.dto.AdminTermsDto;

public interface AdminTermsRepository {
	AdminTermsDto save(AdminTermsDto terms); // 약관 등록
}

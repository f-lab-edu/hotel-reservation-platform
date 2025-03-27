package com.reservation.admin.terms.service;

import org.springframework.stereotype.Service;

import com.reservation.admin.terms.controller.dto.AdminCreateTermsRequest;
import com.reservation.commonapi.terms.repository.AdminTermsRepository;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class TermsService {
	private static final int MAX_TERMS_SAVE_OPTIMISTIC_LOCK_RETRY_COUNT = 3;

	private final AdminTermsRepository adminTermsRepository;

	public Long createTerms(@Valid AdminCreateTermsRequest request) {
		return 1L;
	}
}

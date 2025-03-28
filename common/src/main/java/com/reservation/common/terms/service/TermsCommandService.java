package com.reservation.common.terms.service;

import org.springframework.stereotype.Service;

import com.reservation.common.terms.repository.JpaTermsRepository;
import com.reservation.commonmodel.terms.TermsCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TermsCommandService {

	private final JpaTermsRepository jpaTermsRepository;

	public void deprecateWithoutIncrement(TermsCode code) {
		jpaTermsRepository.deprecateWithoutIncrement(code);
	}
}

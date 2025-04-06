package com.reservation.common.terms.service;

import org.springframework.stereotype.Service;

import com.reservation.common.terms.domain.Terms;
import com.reservation.common.terms.repository.JpaTermsRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TermsCommandService {

	private final JpaTermsRepository jpaTermsRepository;

	public void deprecateTerms(Long id) {
		jpaTermsRepository.findById(id).ifPresent(Terms::deprecate);
	}
}

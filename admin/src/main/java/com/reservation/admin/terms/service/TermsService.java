package com.reservation.admin.terms.service;

import org.springframework.stereotype.Service;

import com.reservation.admin.terms.controller.dto.AdminCreateTermsRequest;

import jakarta.validation.Valid;

@Service
public class TermsService {
	public Long createTerms(@Valid AdminCreateTermsRequest request) {
		return 1L;
	}
}

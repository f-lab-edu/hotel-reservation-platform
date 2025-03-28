package com.reservation.common.terms.domain.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.reservation.common.terms.domain.Terms;
import com.reservation.common.terms.repository.JpaTermsRepository;
import com.reservation.common.terms.service.TermsCommandService;

@ExtendWith(MockitoExtension.class)
public class TermsCommandServiceTest {

	@Mock
	private JpaTermsRepository jpaTermsRepository;

	@InjectMocks
	private TermsCommandService termsCommandService;

	private Terms terms;

	@BeforeEach
	void setUp() {
		terms = mock(Terms.class);
	}

	@Test
	void deprecateTerms_성공() {
		when(jpaTermsRepository.findById(anyLong())).thenReturn(Optional.of(terms));

		termsCommandService.deprecateTerms(1L);

		verify(terms, times(1)).deprecate();
	}

	@Test
	void deprecateTerms_실패_존재하지않는ID() {
		when(jpaTermsRepository.findById(anyLong())).thenReturn(Optional.empty());

		termsCommandService.deprecateTerms(1L);

		verify(terms, never()).deprecate();
	}
}

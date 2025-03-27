package com.reservation.admin.terms.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.reservation.admin.terms.controller.dto.AdminCreateClauseRequest;
import com.reservation.admin.terms.controller.dto.AdminCreateTermsRequest;

@ExtendWith(MockitoExtension.class)
public class TermsServiceTest {
	@InjectMocks
	private TermsService termsService;

	@Test
	void 약관저장_성공ID반환() {
		// given
		AdminCreateTermsRequest request = new AdminCreateTermsRequest(
			"TERMS_USE",
			"서비스 이용약관",
			"REQUIRED",
			"ACTIVE",
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			List.of(
				new AdminCreateClauseRequest(1, "제1조 (목적)", "이 약관은..."),
				new AdminCreateClauseRequest(2, "제2조 (정의)", "여기서 사용하는 용어는...")
			)
		);

		Long id = termsService.createTerms(request);

		assertThat(id).isEqualTo(1L);
	}
}

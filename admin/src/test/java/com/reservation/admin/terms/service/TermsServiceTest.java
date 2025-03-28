package com.reservation.admin.terms.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.reservation.admin.terms.controller.dto.AdminCreateClauseRequest;
import com.reservation.admin.terms.controller.dto.AdminCreateTermsRequest;
import com.reservation.common.exception.BusinessException;
import com.reservation.commonapi.terms.repository.AdminTermsRepository;
import com.reservation.commonapi.terms.repository.dto.AdminClauseDto;
import com.reservation.commonapi.terms.repository.dto.AdminTermsDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

@ExtendWith(MockitoExtension.class)
public class TermsServiceTest {

	@Mock
	private AdminTermsRepository adminTermsRepository;

	@InjectMocks
	private TermsService termsService;

	private AdminCreateTermsRequest request;

	@BeforeEach
	void setUp() {
		request = new AdminCreateTermsRequest(
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			List.of(
				new AdminCreateClauseRequest(1, "제1조 (목적)", "이 약관은..."),
				new AdminCreateClauseRequest(2, "제2조 (정의)", "여기서 사용하는 용어는...")
			)
		);
	}

	@Test
	void 약관저장_성공ID반환() {
		when(adminTermsRepository.findMaxVersionByCode(any(TermsCode.class))).thenReturn(Optional.of(0));
		AdminTermsDto adminTermsDto = new AdminTermsDto(1L,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			1,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			null,
			null,
			List.of(
				new AdminClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
				new AdminClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
			));
		when(adminTermsRepository.save(any(AdminTermsDto.class))).thenReturn(adminTermsDto);

		Long id = termsService.createTerms(request);

		assertThat(id).isEqualTo(1L);
	}

	@Test
	void 약관저장_실패_동일한약관존재() {
		when(adminTermsRepository.findMaxVersionByCode(any(TermsCode.class))).thenReturn(Optional.of(0));
		when(adminTermsRepository.save(any(AdminTermsDto.class))).thenThrow(DataIntegrityViolationException.class);

		BusinessException businessException = assertThrows(BusinessException.class, () -> {
			termsService.createTerms(request);
		});

		assertThat(businessException.getMessage()).isEqualTo("동일한 약관이 이미 등록되었습니다.");
	}

	@Test
	void 약관저장_실패_이미사용중인약관존재() {
		when(adminTermsRepository.existsByCodeAndStatus(any(TermsCode.class), eq(TermsStatus.ACTIVE))).thenReturn(true);

		BusinessException businessException = assertThrows(BusinessException.class, () -> {
			termsService.createTerms(request);
		});

		assertThat(businessException.getMessage()).isEqualTo("이미 사용 중인 약관이 존재합니다. 기존 약관을 수정하세요.");
	}
}
